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
    public Database(int remotePort, String dbPassword, String schemaName) {
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
        // TODO
    }

    /**
     * Lists all classes with their enrolled student counts.
     * Called by: handleListClasses()
     */
    public void listClassesWithStudents() {
        // TODO
    }

    /**
     * Finds and returns the ID of a class matching the given filters.
     * - 1 arg supplied: most recent term, fails if multiple sections exist
     * - 2 args supplied: given term, fails if multiple sections exist
     * - 3 args supplied: exact match on course + term + section
     * Called by: handleSelectClass() -> sets activeClassId
     *
     * @param courseNum course number, always provided
     * @param term      term string, or null if not provided
     * @param section   section number, or null if not provided
     * @return the class_id of the matched class
     */
    public int selectClass(String courseNum, String term, Integer section) {
        // TODO
        return -1;
    }

    /**
     * Displays details of the currently active class.
     * Called by: handleShowClass()
     *
     * @param activeClassId the currently active class ID
     */
    public void showClass(int activeClassId) {
        // TODO
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
        // TODO
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
        // TODO
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