import java.sql.*;
import java.util.Arrays;

/**
 * The Database class handles all interactions with the database.
 *
 * @author Kaleb VanderSys, Zachary Johnston
 * 4-29-2026
 */
public class Database {

    private Statement statement;
    private Connection connection;

    /**
     * Constructor
     * Initializes database connection
     *
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
            // Case 1: only courseNum given, find most recent term
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

            // Case 2: courseNum + term given, find the only section
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

            // Case 3: exact match on course + term + section
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
        String insertCategory = "INSERT INTO Category (name) VALUES (?)";
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
            // If anything fails, undo both inserts
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Failed to add category: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to restore autocommit: " + e.getMessage());
            }
        }
    }

    /**
     * Lists all assignments grouped by category for the active class.
     * Called by: handleShowAssignments()
     *
     * @param activeClassId the currently active class ID
     */
    public void showAssignments(int activeClassId) {
        String sql = """
                SELECT cat.name AS category_name, a.name AS assignment_name,
                       a.description, a.point_value
                FROM Assignment a
                JOIN Category cat ON a.Category_ID = cat.ID
                WHERE a.Class_ID = ?
                ORDER BY cat.name, a.name
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, activeClassId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No assignments found for this class.");
                    return;
                }

                String currentCategory = null;

                while (rs.next()) {
                    String category = rs.getString("category_name");

                    // Print category header whenever for each new category
                    if (!category.equals(currentCategory)) {
                        currentCategory = category;
                        System.out.println("\n[ " + category + " ]");
                        System.out.printf("  %-25s %-8s %s%n", "Assignment", "Points", "Description");
                        System.out.println("  " + "-".repeat(55));
                    }

                    System.out.printf("  %-25s %-8.0f %s%n",
                            rs.getString("assignment_name"),
                            rs.getDouble("point_value"),
                            rs.getString("description"));
                }
                System.out.println();
            }

        } catch (SQLException e) {
            System.err.println("Failed to show assignments: " + e.getMessage());
        }
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
        String lookupSql = """
                SELECT cat.ID
                FROM Category cat
                JOIN ClassHasCategory chc ON cat.ID = chc.Category_ID
                WHERE chc.Class_ID = ? AND cat.name = ?
                """;

        String insertSql = """
                INSERT INTO Assignment (name, description, point_value, Category_ID, Class_ID)
                VALUES (?, ?, ?, ?, ?)
                """;

        try {
            // Step 1: find the category
            int categoryId;
            try (PreparedStatement ps = connection.prepareStatement(lookupSql)) {
                ps.setInt(1, activeClassId);
                ps.setString(2, category);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Category '" + category + "' not found in this class. "
                                + "Use 'show-categories' to see available categories.");
                        return;
                    }
                    categoryId = rs.getInt("ID");
                }
            }

            // Step 2: insert the assignment
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setString(1, name);
                ps.setString(2, description);
                ps.setInt(3, points);
                ps.setInt(4, categoryId);
                ps.setInt(5, activeClassId);
                ps.executeUpdate();
                System.out.println("Assignment '" + name + "' added to category '" + category
                        + "' worth " + points + " points.");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("An assignment named '" + name + "' already exists in this class.");
            } else {
                System.err.println("Failed to add assignment: " + e.getMessage());
            }
        }
    }

    //////////////////////////////////
    //      Student Management      //
    //////////////////////////////////

    /**
     * /**
     * Adds a new student and enrolls them in the active class.
     * If the student already exists by username:
     * - same name: just enrolls them
     * - different name: updates the name with a warning, then enrolls
     * Called by: handleAddStudent() when 4 args are given
     *
     * @param activeClassId the currently active class ID
     * @param username      student username e.g. "jsmith"
     * @param studentId     student ID number (not stored in current schema)
     * @param lastName      student last name
     * @param firstName     student first name
     */
    public void addStudent(int activeClassId, String username, String studentId, String lastName, String firstName) {
        String fullName = firstName + " " + lastName;

        String lookupSql = "SELECT ID, name FROM Student WHERE username = ?";
        String updateSql = "UPDATE Student SET name = ? WHERE username = ?";
        String insertSql = "INSERT INTO Student (ID, name, username) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            // Step 1: check if student already exists
            try (PreparedStatement ps = connection.prepareStatement(lookupSql)) {
                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Student already exists
                        int existingId = rs.getInt("ID");
                        String existingName = rs.getString("name");

                        if (!existingName.equals(fullName)) {
                            // Name mismatch — warn and update
                            System.out.println("Warning: name for '" + username + "' is changing from '"
                                    + existingName + "' to '" + fullName + "'.");
                            try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                                update.setString(1, fullName);
                                update.setString(2, username);
                                update.executeUpdate();
                            }
                        }

                        // Enroll in class (student already exists, just link them)
                        enrollStudentById(activeClassId, existingId, username);
                        connection.commit();
                        System.out.println("Student '" + username + "' enrolled in class.");
                        return;
                    }
                }
            }

            // Step 2: student doesn't exist — insert them
            int newStudentId = parseStudentId(studentId);

            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setInt(1, newStudentId);
                ps.setString(2, fullName);
                ps.setString(3, username);
                ps.executeUpdate();
            }

            // Step 3: enroll the newly created student
            enrollStudentById(activeClassId, newStudentId, username);
            connection.commit();
            System.out.println("Student '" + fullName + "' (" + username + ") added and enrolled.");

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            System.err.println("Failed to add student: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            try { connection.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            System.out.println("Error: " + e.getMessage());
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { System.err.println("Failed to restore autocommit: " + e.getMessage()); }
        }
    }

    /**
     * Enrolls an already-existing student in the active class by username.
     * Prints an error if the student does not exist.
     * Called by: handleAddStudent() when only 1 arg is given
     *
     * @param activeClassId the currently active class ID
     * @param username      student username to enroll
     */
    public void enrollStudent(int activeClassId, String username) {
        String lookupSql = "SELECT ID FROM Student WHERE username = ?";

        try (PreparedStatement ps = connection.prepareStatement(lookupSql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No student found with username '" + username + "'. "
                            + "Use 'add-student <username> <id> <last> <first>' to create them first.");
                    return;
                }
                int studentId = rs.getInt("ID");
                enrollStudentById(activeClassId, studentId, username);
            }

        } catch (SQLException e) {
            System.err.println("Failed to enroll student: " + e.getMessage());
        }
    }

    /**
     * Internal helper: inserts a row into Enrolled for a given class + student.
     * Handles the case where the student is already enrolled gracefully.
     *
     * @param activeClassId the class to enroll into
     * @param studentId     the Student.ID to enroll
     * @param username      username (used for print messages only)
     */
    private void enrollStudentById(int activeClassId, int studentId, String username) throws SQLException {
        String sql = "INSERT INTO Enrolled (Class_ID, Student_ID) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, activeClassId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
            System.out.println("Student '" + username + "' enrolled in class " + activeClassId + ".");
        } catch (SQLException e) {
            // Primary key violation means they're already enrolled
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("Student '" + username + "' is already enrolled in this class.");
            } else {
                throw e;
            }
        }
    }

    /**
     * Checks whether a student exists in the students table.
     * Can be used internally by addStudent() to decide whether to insert or update.
     *
     * @param username student username to check
     * @return true if the student exists, false otherwise
     */
    public boolean studentExists(String username) {
        String sql = "SELECT ID FROM Student WHERE username = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Failed to check if student exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Shows all students enrolled in the active class.
     * Called by: handleShowStudents() when no search string is given
     *
     * @param activeClassId the currently active class ID
     */
    public void getStudents(int activeClassId) {
        getStudents(activeClassId, null);
    }

    /**
     * Shows students in the active class whose name or username contains the search string.
     * Called by: handleShowStudents() when a search string is given
     *
     * @param activeClassId the currently active class ID
     * @param search        case-insensitive substring to match, or null for all students
     */
    public void getStudents(int activeClassId, String search) {
        String sql = """
                SELECT s.username, s.name
                FROM Student s
                JOIN Enrolled e ON s.ID = e.Student_ID
                WHERE e.Class_ID = ?
                """ + (search != null ? "AND (s.name LIKE ? OR s.username LIKE ?) " : "")
                + "ORDER BY s.name";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, activeClassId);

            if (search != null) {
                String pattern = "%" + search + "%";
                ps.setString(2, pattern);
                ps.setString(3, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println(search != null
                            ? "No students found matching '" + search + "'."
                            : "No students enrolled in this class.");
                    return;
                }

                System.out.printf("%-20s %s%n", "Username", "Name");
                System.out.println("-".repeat(40));

                while (rs.next()) {
                    System.out.printf("%-20s %s%n",
                            rs.getString("username"),
                            rs.getString("name"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to get students: " + e.getMessage());
        }
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

        String assignmentLookupSql = """
                SELECT ID, point_value
                FROM Assignment
                WHERE name = ? AND Class_ID = ?
                """;

        String studentLookupSql = """
                SELECT s.ID
                FROM Student s
                JOIN Enrolled e ON s.ID = e.Student_ID
                WHERE s.username = ? AND e.Class_ID = ?
                """;

        String upsertSql = """
                INSERT INTO Assigned (Student_ID, Assignment_ID, grade)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE grade = VALUES(grade)
                """;

        try {
            // Step 1: look up assignment, make sure it belongs to this class
            int assignmentId;
            double maxPoints;
            try (PreparedStatement ps = connection.prepareStatement(assignmentLookupSql)) {
                ps.setString(1, assignmentName);
                ps.setInt(2, activeClassId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Assignment '" + assignmentName + "' not found in this class. "
                                + "Use 'show-assignments' to see available assignments.");
                        return;
                    }
                    assignmentId = rs.getInt("ID");
                    maxPoints = rs.getDouble("point_value");
                }
            }

            // Step 2: warn if grade exceeds max points
            if (grade > maxPoints) {
                System.out.printf("Warning: grade %.0f exceeds max points %.0f for '%s'.%n",
                        grade, maxPoints, assignmentName);
            }

            // Step 3: look up student, make sure they're enrolled in this class
            int studentId;
            try (PreparedStatement ps = connection.prepareStatement(studentLookupSql)) {
                ps.setString(1, username);
                ps.setInt(2, activeClassId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Student '" + username + "' not found or not enrolled in this class.");
                        return;
                    }
                    studentId = rs.getInt("ID");
                }
            }

            // Step 4: insert or replace the grade
            try (PreparedStatement ps = connection.prepareStatement(upsertSql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, assignmentId);
                ps.setDouble(3, grade);
                ps.executeUpdate();
                System.out.printf("Grade %.0f assigned to '%s' for '%s'.%n",
                        grade, username, assignmentName);
            }

        } catch (SQLException e) {
            System.err.println("Failed to assign grade: " + e.getMessage());
        }
    }

    ///////////////////////////////
    //      Grade Reporting      //
    ///////////////////////////////

    /**
     * Shows all grades for a student grouped by category, with subtotals and overall grade.
     * Reports both total grade (ungraded = 0) and attempted grade (graded only).
     * Called by: handleStudentGrades()
     *
     * @param activeClassId the currently active class ID
     * @param username      student username
     */
    public void getGradesForStudent(int activeClassId, String username) {

        // First verify the student exists and is enrolled
        String studentLookupSql = """
                SELECT s.ID, s.name
                FROM Student s
                JOIN Enrolled e ON s.ID = e.Student_ID
                WHERE s.username = ? AND e.Class_ID = ?
                """;

        // Pull all assignments for the class with the student's grade (null if ungraded)
        // Also pull category weight so we can compute weighted grade
        String gradesSql = """
                SELECT
                    cat.name        AS category_name,
                    chc.weight      AS category_weight,
                    a.name          AS assignment_name,
                    a.point_value   AS max_points,
                    asn.grade       AS earned
                FROM Assignment a
                JOIN Category cat ON a.Category_ID = cat.ID
                JOIN ClassHasCategory chc ON cat.ID = chc.Category_ID
                    AND chc.Class_ID = a.Class_ID
                LEFT JOIN Assigned asn ON a.ID = asn.Assignment_ID
                    AND asn.Student_ID = ?
                WHERE a.Class_ID = ?
                ORDER BY cat.name, a.name
                """;

        // Get total weight so we can rescale to 100
        String totalWeightSql = """
                SELECT SUM(weight) AS total_weight
                FROM ClassHasCategory
                WHERE Class_ID = ?
                """;

        try {
            // Step 1: verify student is enrolled
            int studentId;
            String studentName;
            try (PreparedStatement ps = connection.prepareStatement(studentLookupSql)) {
                ps.setString(1, username);
                ps.setInt(2, activeClassId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Student '" + username + "' not found or not enrolled in this class.");
                        return;
                    }
                    studentId = rs.getInt("ID");
                    studentName = rs.getString("name");
                }
            }

            // Step 2: get total weight for rescaling
            double totalWeight;
            try (PreparedStatement ps = connection.prepareStatement(totalWeightSql)) {
                ps.setInt(1, activeClassId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    totalWeight = rs.getDouble("total_weight");
                }
            }

            // Step 3: fetch all assignments + grades and display grouped by category
            try (PreparedStatement ps = connection.prepareStatement(gradesSql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, activeClassId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.isBeforeFirst()) {
                        System.out.println("No assignments found for this class.");
                        return;
                    }

                    System.out.println("\nGrades for " + studentName + " (" + username + ")");
                    System.out.println("=".repeat(60));

                    // Accumulators for overall grade
                    double totalEarned = 0;
                    double attemptEarned = 0;

                    // Per-category accumulators (reset each category)
                    String currentCategory = null;
                    double catWeight = 0;
                    double catMaxTotal = 0;
                    double catEarnedTotal = 0;
                    double catAttemptMax = 0;
                    double catAttemptEarned = 0;

                    // Store current row data to process after detecting category change
                    boolean firstRow = true;

                    // Buffer: re-read into local vars each iteration
                    String rowCategory, rowAssignment;
                    double rowWeight, rowMax;
                    Double rowEarned;

                    // Use a do-while pattern by peeking ahead isn't easy with ResultSet,
                    // so we flush category totals when category changes or at end
                    while (rs.next()) {
                        rowCategory = rs.getString("category_name");
                        rowWeight = rs.getDouble("category_weight");
                        rowAssignment = rs.getString("assignment_name");
                        rowMax = rs.getDouble("max_points");
                        rowEarned = rs.wasNull() ? null : rs.getDouble("earned");

                        // Flush previous category when we enter a new one
                        if (!rowCategory.equals(currentCategory)) {
                            if (currentCategory != null) {
                                double[] subtotals = flushCategory(currentCategory, catWeight,
                                        totalWeight, catMaxTotal, catEarnedTotal,
                                        catAttemptMax, catAttemptEarned);
                                totalEarned += subtotals[0];
                                attemptEarned += subtotals[1];
                            }

                            // Start new category
                            currentCategory = rowCategory;
                            catWeight = rowWeight;
                            catMaxTotal = 0;
                            catEarnedTotal = 0;
                            catAttemptMax = 0;
                            catAttemptEarned = 0;

                            System.out.println("\n[ " + currentCategory
                                    + " | weight: " + String.format("%.1f", rowWeight) + "% ]");
                            System.out.printf("  %-25s %-10s %s%n", "Assignment", "Earned", "Max");
                            System.out.println("  " + "-".repeat(45));
                        }

                        // Print this assignment row
                        String earnedStr = rowEarned != null
                                ? String.format("%.0f", rowEarned)
                                : "--";
                        System.out.printf("  %-25s %-10s %.0f%n",
                                rowAssignment, earnedStr, rowMax);

                        // Accumulate category totals
                        catMaxTotal += rowMax;
                        catEarnedTotal += rowEarned != null ? rowEarned : 0;
                        if (rowEarned != null) {
                            catAttemptMax += rowMax;
                            catAttemptEarned += rowEarned;
                        }
                    }

                    // Flush the last category
                    if (currentCategory != null) {
                        double[] subtotals = flushCategory(currentCategory, catWeight,
                                totalWeight, catMaxTotal, catEarnedTotal,
                                catAttemptMax, catAttemptEarned);
                        totalEarned += subtotals[0];
                        attemptEarned += subtotals[1];
                    }

                    // Print overall grades
                    System.out.println("\n" + "=".repeat(60));
                    System.out.printf("  Total grade:     %.2f%%%n", totalEarned);
                    System.out.printf("  Attempted grade: %.2f%%%n", attemptEarned);
                    System.out.println("=".repeat(60));
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to get student grades: " + e.getMessage());
        }
    }

    /**
     * Prints the subtotal line for a completed category and returns the
     * weighted contribution to the overall total and attempted grades.
     *
     * @return double[2] where [0] = weighted total contribution,
     * [1] = weighted attempted contribution
     */
    private double[] flushCategory(String name, double weight, double totalWeight,
                                   double maxTotal, double earnedTotal,
                                   double attemptMax, double attemptEarned) {
        double rescaledWeight = (totalWeight > 0) ? (weight / totalWeight) * 100.0 : 0;
        double catTotalScore = (maxTotal > 0) ? (earnedTotal / maxTotal) * rescaledWeight : 0;
        double catAttemptScore = (attemptMax > 0) ? (attemptEarned / attemptMax) * rescaledWeight : 0;

        System.out.println("  " + "-".repeat(45));
        System.out.printf("  Subtotal: %.0f / %.0f pts  →  %.2f%% (of %.1f%% weight)%n",
                earnedTotal, maxTotal, catTotalScore, rescaledWeight);

        return new double[]{catTotalScore, catAttemptScore};
    }

    /**
     * Shows the full gradebook for the active class: all students with their total grades.
     * Reports both total grade and attempted grade for each student.
     * Called by: handleGradebook()
     *
     * @param activeClassId the currently active class ID
     */
    public void getCurrentClassGrades(int activeClassId) {

        // Get total weight for rescaling
        String totalWeightSql = """
                SELECT SUM(weight) AS total_weight
                FROM ClassHasCategory
                WHERE Class_ID = ?
                """;

        // For each student, compute their weighted total and attempted scores in SQL.
        String gradebookSql = """
                SELECT
                    s.username,
                    s.name,
                    cat.name                        AS category_name,
                    chc.weight                      AS category_weight,
                    SUM(a.point_value)              AS cat_max,
                    SUM(COALESCE(asn.grade, 0))     AS cat_earned,
                    SUM(CASE WHEN asn.grade IS NOT NULL THEN a.point_value ELSE 0 END)
                                                    AS cat_attempt_max,
                    SUM(CASE WHEN asn.grade IS NOT NULL THEN asn.grade ELSE 0 END)
                                                    AS cat_attempt_earned
                FROM Student s
                JOIN Enrolled e     ON s.ID = e.Student_ID AND e.Class_ID = ?
                JOIN Assignment a   ON a.Class_ID = ?
                JOIN Category cat   ON a.Category_ID = cat.ID
                JOIN ClassHasCategory chc ON cat.ID = chc.Category_ID AND chc.Class_ID = ?
                LEFT JOIN Assigned asn ON asn.Assignment_ID = a.ID AND asn.Student_ID = s.ID
                GROUP BY s.ID, s.username, s.name, cat.ID, cat.name, chc.weight
                ORDER BY s.name, cat.name
                """;

        try {
            // Step 1: get total weight for rescaling
            double totalWeight;
            try (PreparedStatement ps = connection.prepareStatement(totalWeightSql)) {
                ps.setInt(1, activeClassId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    totalWeight = rs.getDouble("total_weight");
                }
            }

            if (totalWeight == 0) {
                System.out.println("No categories with weights found for this class.");
                return;
            }

            // Step 2: fetch per-student per-category rows and accumulate
            try (PreparedStatement ps = connection.prepareStatement(gradebookSql)) {
                ps.setInt(1, activeClassId);
                ps.setInt(2, activeClassId);
                ps.setInt(3, activeClassId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.isBeforeFirst()) {
                        System.out.println("No students enrolled in this class.");
                        return;
                    }

                    // Print header
                    System.out.println("\nGradebook");
                    System.out.println("=".repeat(65));
                    System.out.printf("%-20s %-12s %-15s %s%n",
                            "Username", "Student ID", "Total Grade", "Attempted Grade");
                    System.out.println("-".repeat(65));

                    // Accumulators per student (reset when username changes)
                    String currentUsername = null;
                    String currentName = null;
                    double totalScore = 0;
                    double attemptScore = 0;

                    while (rs.next()) {
                        String username = rs.getString("username");
                        String name = rs.getString("name");
                        double catWeight = rs.getDouble("category_weight");
                        double catMax = rs.getDouble("cat_max");
                        double catEarned = rs.getDouble("cat_earned");
                        double catAttemptMax = rs.getDouble("cat_attempt_max");
                        double catAttemptEarned = rs.getDouble("cat_attempt_earned");

                        // Flush previous student when username changes
                        if (!username.equals(currentUsername)) {
                            if (currentUsername != null) {
                                printGradebookRow(currentUsername, currentName, totalScore, attemptScore);
                            }
                            currentUsername = username;
                            currentName = name;
                            totalScore = 0;
                            attemptScore = 0;
                        }

                        // Rescale this category's weight and accumulate
                        double rescaled = (catWeight / totalWeight) * 100.0;
                        totalScore += catMax > 0 ? (catEarned / catMax) * rescaled : 0;
                        attemptScore += catAttemptMax > 0 ? (catAttemptEarned / catAttemptMax) * rescaled : 0;
                    }

                    // Flush last student
                    if (currentUsername != null) {
                        printGradebookRow(currentUsername, currentName, totalScore, attemptScore);
                    }

                    System.out.println("=".repeat(65));
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to get gradebook: " + e.getMessage());
        }
    }

    /**
     * Prints a single student row in the gradebook.
     *
     * @param username     student username
     * @param name         student full name
     * @param totalScore   weighted total grade (ungraded = 0)
     * @param attemptScore weighted attempted grade (graded only)
     */
    private void printGradebookRow(String username, String name, double totalScore, double attemptScore) {
        System.out.printf("%-20s %-30s %6.2f%%  %10.2f%%%n",
                username, name, totalScore, attemptScore);
    }

    //////////////////////////////
    //      Helper Methods      //
    //////////////////////////////

    /**
     * Executes a SQL SELECT query and returns the ResultSet.
     * Prints and rethrows any SQLException as a RuntimeException.
     *
     * Decided not to use because Prepared statement is better
     * Added Transactions to methods where commits are connected
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

    /**
     * Parses studentId string to int, throwing a clear error if invalid.
     */
    private int parseStudentId(String studentId) {
        try {
            return Integer.parseInt(studentId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Student ID must be a number, got: " + studentId);
        }
    }
}