import java.sql.*;
import java.util.Arrays;

/**
 * The Database class handles all interactions with the database.
 * @author Kaleb VanderSys, Zachary Johnston
 * 4-29-2026
 */
public class Database {

    private Statement statement;
    private Connection connection;

    /**
     * Constructor
     *  Initializes database connection
     * @param remotePort - the port the mysql database is one
     * @param dbPassword - the password to your database
     * @param schemaName - the name of the target schema
     */
    public Database(String remotePort, String dbPassword, String schemaName) {
        //Load driver class file
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:" + remotePort + "/" + schemaName + "?verifyServerCertificate=false&useSSL=true&maxAllowedPacket=65535", "msandbox", dbPassword);
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.err.println("Failed to establish connection to database");
            System.err.println(e.getMessage());
            System.err.println("Stack Trace: " + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            System.err.println("Error with jdbc Driver dependency");
            System.err.println(e.getMessage());
            System.err.println("Stack Trace: " + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    /**
     * Used when closing the program i.e. an "exit" command is entered
     */
    public void closeDatabaseConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("WARNING FAILED TO CLOSE DATABASE CONNECTION (make sure this was used on an open connection).");
            System.err.println(e.getMessage());
            System.err.println("Stack Trace: " + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    //////////////////////////////
    //      Class Management    //
    //////////////////////////////

    /**
     * Creates a new class in the database.
     * Called by: handleNewClass()
     *
     * @param courseNum   e.g. "CS410"
     * @param term        e.g. "Sp20"
     * @param section     e.g. 1
     * @param description e.g. "Databases"
     */
    public void createClass(String courseNum, String term, int section, String description) {
        String sql = "INSERT INTO Class (course_num, term, section_num, description) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, courseNum);
            ps.setString(2, term);
            ps.setString(3, String.valueOf(section));
            ps.setString(4, description);
            ps.executeUpdate();
            System.out.println("Class created: " + courseNum + " " + term + " section " + section);
        } catch (SQLException e) {
            System.err.println("Failed to create class: " + e.getMessage());
        }
    }

    /**
     * Lists all classes with their enrolled student counts.
     * Called by: handleListClasses()
     */
    public void listClassesWithStudents() {
        String sql = """
            SELECT c.course_num, c.term, c.section_num, c.description,
                   COUNT(e.Student_ID) AS student_count
            FROM Class c
            LEFT JOIN Enrolled e ON c.ID = e.Class_ID
            GROUP BY c.ID, c.course_num, c.term, c.section_num, c.description
            ORDER BY c.term, c.course_num, c.section_num
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No classes found.");
                return;
            }

            System.out.printf("%-8s %-6s %-8s %-4s %s%n",
                    "Course", "Term", "Section", "Students", "Description");
            System.out.println("-".repeat(60));

            while (rs.next()) {
                System.out.printf("%-8s %-6s %-8s %-4d %s%n",
                        rs.getString("course_num"),
                        rs.getString("term"),
                        rs.getString("section_num"),
                        rs.getInt("student_count"),
                        rs.getString("description"));
            }

        } catch (SQLException e) {
            System.err.println("Failed to list classes: " + e.getMessage());
        }
    }

    /**
     * Finds the class ID matching the given filters and sets it as active.
     * - 1 arg: most recent term for that course, fails if multiple sections
     * - 2 args: given term for that course, fails if multiple sections
     * - 3 args: exact match on course + term + section, fails if not found
     * Called by: handleSelectClass() -> sets activeClassId
     *
     * @param courseNum course number, always provided
     * @param term      term string, or null if not provided
     * @param section   section number, or null if not provided
     * @return the ID of the matched class
     */
    public int selectClass(String courseNum, String term, Integer section) {

        try {
            // --- Case 1: only courseNum given, find most recent term ---
            if (term == null) {
                String sql = """
                    SELECT ID, term, section_num
                    FROM Class
                    WHERE course_num = ?
                    AND term = (
                        SELECT term FROM Class
                        WHERE course_num = ?
                        ORDER BY ID DESC
                        LIMIT 1
                    )
                    """;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, courseNum);
                    ps.setString(2, courseNum);
                    return expectExactlyOne(ps, courseNum, null, null);
                }
            }

            // --- Case 2: courseNum + term given, find the only section ---
            if (section == null) {
                String sql = """
                    SELECT ID, term, section_num
                    FROM Class
                    WHERE course_num = ? AND term = ?
                    """;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, courseNum);
                    ps.setString(2, term);
                    return expectExactlyOne(ps, courseNum, term, null);
                }
            }

            // --- Case 3: exact match on course + term + section ---
            String sql = """
                SELECT ID, term, section_num
                FROM Class
                WHERE course_num = ? AND term = ? AND section_num = ?
                """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, courseNum);
                ps.setString(2, term);
                ps.setString(3, String.valueOf(section));
                return expectExactlyOne(ps, courseNum, term, section);
            }

        } catch (SQLException e) {
            System.err.println("Failed to select class: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Runs a PreparedStatement that should return exactly one class row.
     * Prints a clear error and returns -1 if zero or multiple rows are found.
     *
     * @param ps      the already-parameterized PreparedStatement to execute
     * @param course  course num (for error messages)
     * @param term    term (for error messages, may be null)
     * @param section section (for error messages, may be null)
     * @return the class ID, or -1 on failure
     */
    private int expectExactlyOne(PreparedStatement ps, String course, String term, Integer section)
            throws SQLException {

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                System.out.println("No class found for: " + course
                        + (term != null ? " " + term : "")
                        + (section != null ? " section " + section : ""));
                return -1;
            }

            int classId = rs.getInt("ID");
            String foundTerm = rs.getString("term");
            String foundSection = rs.getString("section_num");

            // Check if there's more than one result
            if (rs.next()) {
                System.out.println("Multiple sections found for " + course
                        + (term != null ? " " + term : " (most recent term: " + foundTerm + ")")
                        + ". Please be more specific.");
                return -1;
            }

            System.out.println("Selected: " + course + " " + foundTerm + " section " + foundSection);
            return classId;
        }
    }

    /**
     * Displays details of the currently active class.
     * Called by: handleShowClass()
     *
     * @param activeClassId the currently active class ID
     */
    public void showClass(int activeClassId) {
        String sql = "SELECT course_num, term, section_num, description FROM Class WHERE ID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, activeClassId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No class found with ID " + activeClassId);
                    return;
                }

                System.out.println("Active Class:");
                System.out.println("  Course:      " + rs.getString("course_num"));
                System.out.println("  Term:        " + rs.getString("term"));
                System.out.println("  Section:     " + rs.getString("section_num"));
                System.out.println("  Description: " + rs.getString("description"));
            }

        } catch (SQLException e) {
            System.err.println("Failed to show class: " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////
    //      Category and Assignment Management      //
    //////////////////////////////////////////////////

    /**
     * Lists all categories and their weights for the active class.
     * Called by: handleShowCategories()
     *
     * @param activeClassId the currently active class ID
     */
    public void showCategories(int activeClassId) {
        String sql = """
            SELECT cat.name, chc.weight
            FROM ClassHasCategory chc
            JOIN Category cat ON chc.Category_ID = cat.ID
            WHERE chc.Class_ID = ?
            ORDER BY cat.name
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, activeClassId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No categories found for this class.");
                    return;
                }

                System.out.printf("%-20s %s%n", "Category", "Weight");
                System.out.println("-".repeat(30));

                while (rs.next()) {
                    System.out.printf("%-20s %.2f%%%n",
                            rs.getString("name"),
                            rs.getDouble("weight"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to show categories: " + e.getMessage());
        }
    }

    /**
     * Adds a new grading category to the active class.
     * Called by: handleAddCategory()
     *
     * @param activeClassId the currently active class ID
     * @param name          category name e.g. "Homework"
     * @param weight        category weight e.g. 40.0
     */
    public void addCategory(int activeClassId, String name, double weight) {
        String insertCategory       = "INSERT INTO Category (name) VALUES (?)";
        String insertClassHasCategory = "INSERT INTO ClassHasCategory (Class_ID, Category_ID, weight) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false); // start transaction

            // Step 1: insert into Category, get the generated ID back
            int categoryId;
            try (PreparedStatement ps = connection.prepareStatement(
                    insertCategory, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Failed to retrieve generated Category ID.");
                    }
                    categoryId = keys.getInt(1);
                }
            }

            // Step 2: link the new category to the active class with its weight
            try (PreparedStatement ps = connection.prepareStatement(insertClassHasCategory)) {
                ps.setInt(1, activeClassId);
                ps.setInt(2, categoryId);
                ps.setDouble(3, weight);
                ps.executeUpdate();
            }

            connection.commit();
            System.out.println("Category '" + name + "' added with weight " + weight + "%");

        } catch (SQLException e) {
            // If anything fails, roll back both inserts
            try { connection.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            System.err.println("Failed to add category: " + e.getMessage());
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { System.err.println("Failed to restore autocommit: " + e.getMessage()); }
        }
    }

    /**
     * Lists all assignments grouped by category for the active class.
     * Called by: handleShowAssignments()
     *
     * @param activeClassId the currently active class ID
     */
    public void showAssignments(int activeClassId) {
        // TODO
    }

    /**
     * Adds a new assignment to the active class.
     * Called by: handleAddAssignment()
     *
     * @param activeClassId the currently active class ID
     * @param name          assignment name e.g. "HW1"
     * @param category      category name e.g. "Homework"
     * @param description   assignment description
     * @param points        total possible points
     */
    public void addAssignment(int activeClassId, String name, String category, String description, int points) {
        // TODO
    }

    //////////////////////////////////
    //      Student Management      //
    //////////////////////////////////

    /**
     * Adds a new student to the students table and enrolls them in the active class.
     * If the student already exists and the name differs, updates the name with a warning.
     * Called by: handleAddStudent() when 4 args are given
     *
     * @param activeClassId the currently active class ID
     * @param username      student username e.g. "jsmith"
     * @param studentId     student ID number e.g. "123456"
     * @param lastName      student last name
     * @param firstName     student first name
     */
    public void addStudent(int activeClassId, String username, String studentId, String lastName, String firstName) {
        // TODO
    }

    /**
     * Enrolls an already-existing student in the active class.
     * Prints an error if the student does not exist.
     * Called by: handleAddStudent() when 1 arg is given
     *
     * @param activeClassId the currently active class ID
     * @param username      student username to enroll
     */
    public void enrollStudent(int activeClassId, String username) {
        // TODO
    }

    /**
     * Checks whether a student exists in the students table.
     * Used internally by addStudent() to decide whether to insert or update.
     *
     * @param username student username to check
     * @return true if the student exists, false otherwise
     */
    public boolean studentExists(String username) {
        // TODO
        return false;
    }

    /**
     * Shows all students enrolled in the active class.
     * Called by: handleShowStudents() when no search string is given
     *
     * @param activeClassId the currently active class ID
     */
    public void getStudents(int activeClassId) {
        // TODO
    }

    /**
     * Shows students in the active class whose name or username contains the search string.
     * Called by: handleShowStudents() when a search string is given
     *
     * @param activeClassId the currently active class ID
     * @param search        case-insensitive substring to match
     */
    public void getStudents(int activeClassId, String search) {
        // TODO
    }

    //////////////////////////////////
    //      Grade Management        //
    //////////////////////////////////

    /**
     * Assigns a grade to a student for an assignment. Replaces any existing grade.
     * Prints a warning if grade exceeds the assignment's max points.
     * Called by: handleGrade()
     *
     * @param activeClassId  the currently active class ID
     * @param assignmentName name of the assignment
     * @param username       student username
     * @param grade          points awarded
     */
    public void assignGrade(int activeClassId, String assignmentName, String username, double grade) {
        // TODO
    }

    ///////////////////////////////
    //      Grade Reporting      //
    ///////////////////////////////

    /**
     * Shows all grades for a student, grouped by category, with subtotals and overall grade.
     * Reports both total grade (ungraded = 0) and attempted grade (graded only).
     * Called by: handleStudentGrades()
     *
     * @param activeClassId the currently active class ID
     * @param username      student username
     */
    public void getGradesForStudent(int activeClassId, String username) {
        // TODO
    }

    /**
     * Shows the full gradebook for the active class: all students with their total grades.
     * Reports both total grade and attempted grade for each student.
     * Called by: handleGradebook()
     *
     * @param activeClassId the currently active class ID
     */
    public void getCurrentClassGrades(int activeClassId) {
        // TODO
    }

    //////////////////////////////
    //      Helper Methods      //
    //////////////////////////////

    /**
     * Executes a SQL SELECT query and returns the ResultSet.
     * Prints and rethrows any SQLException as a RuntimeException.
     *
     * @param query - A SQL Query
     * @return - the ResultSet of the query.
     * @throws SQLException - thrown in the case the connection rollback fails
     */
    private ResultSet executeQuery(String query) throws SQLException {
        ResultSet results;
        try {
            connection.setAutoCommit(false);
           results = statement.executeQuery(query);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        } finally {
            connection.setAutoCommit(true);
        }
        return results;
    }
}