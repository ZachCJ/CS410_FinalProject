import java.sql.*;

public class Database {

    /**
     *
     * Source for jdbc connection: https://www.geeksforgeeks.org/java/establishing-jdbc-connection-in-java/
     */
    public Database() throws ClassNotFoundException {
        //Load driver class file
        Class.forName("oracle.jdbc.OracleDriver");
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        Connection connection = DriverManager.getConnection(url, user, password);
        String url = "jdbc:mysql://*url*:port";
    }

}
