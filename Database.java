import java.sql.*;
import java.util.Arrays;

/**
 * The Database class handles all interactions with the database that the program needs to make
 * @author Kaleb VanderSys, Zachary Johnston
 * 4-29-2026
 */
public class Database {

    private Statement statement;
    private Connection connection;

    /**
     * TODO: Test and finish
     * Source for jdbc connection: https://www.geeksforgeeks.org/java/establishing-jdbc-connection-in-java/
     */
    public Database(String remotePort, String dbPassword) throws ClassNotFoundException, SQLException {
        //Load driver class file
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://onyx.boisestate.edu" + remotePort + "/test?verifyServerCertificate=false&useSSL=true", "msandbox", dbPassword);
        statement = connection.createStatement();
    }

    //////////////////////////////
    //      Class Management    //
    //////////////////////////////

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
     public void createClass() {

    }

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void listClassesWithStudents() {

    }

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void getActiveClasses() {

    }

    //////////////////////////////////////////////////
    //      Category and Assignment Management      //
    //////////////////////////////////////////////////

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void showCategories() {

    }

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void addCategory() {

    }

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void showAssignment() {

    }

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void addAssignment() {

    }

    //////////////////////////////////
    //      Student Management      //
    //////////////////////////////////

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void addStudent() {

    }

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void enrollStudent() {

    }

    /**
     * Note: needed for adding new students to check if a student ID is being updated
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public boolean studentExists() {

    }

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void updateStudent() {

    }

    /**
     * Note: There will be two of these one that uses the current class and one that uses a string match.
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void getStudents() {

    }

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void assignGrade() {

    }

    ///////////////////////////////
    //      Grade Reporting      //
    ///////////////////////////////

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void getGradesForStudent() {

    }

    /**
     * TODO: Rewrite method signature to include params and proper return, and make
     */
    public void getCurrentClassGrades() {

    }

    //////////////////////////////
    //      Helper Methods      //
    //////////////////////////////

    /**
     * Takes an input SQL Query and returns its result set. If there is an issue with the query
     *  this method will print out the error relating to the afflicting query and propagate the
     *  exception back in the stack.
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
    }

}
