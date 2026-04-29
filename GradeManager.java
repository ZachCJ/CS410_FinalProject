import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GradeManager {

    Database db;

    // -------------------------------------------------------------------------
    // Tokenizer - splits a raw input line into tokens, respecting quoted strings
    //   e.g.  new-class CS410 Sp20 1 "Databases Fall 2020"
    //         → ["new-class", "CS410", "Sp20", "1", "Databases Fall 2020"]
    // -------------------------------------------------------------------------
    public static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Toggle quote mode; don't include the quote char itself
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                // Space outside quotes → end of token
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        // Don't forget the last token
        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    public void dispatch(List<String> tokens) {
        if (tokens.isEmpty()) return;

        String cmd = tokens.get(0).toLowerCase();
        // args = everything after the command name
        List<String> args = tokens.subList(1, tokens.size());

        try {
            switch (cmd) {

                // --- Class management ---
                case "new-class":
                    System.out.println("new-class placeholder");
//                    db.createClass();
                    break;
                case "list-classes":
                    System.out.println("list-class placeholder");
//                    db.listClassesWithStudents();
                    break;
                case "select-class":
                    break;
                case "show-class":
//                    classManager.showClass();
                    break;
                // --- Category management ---
                case "show-categories":
//                    categoryManager.showCategories();
                    break;
                case "add-category":
//                    categoryManager.addCategory(args);
                    break;

                // --- Assignment management ---
                case "show-assignments":
//                    categoryManager.showAssignments();
                    break;
                case "add-assignment":
//                    categoryManager.addAssignment(args);
                    break;

                // --- Student management ---
                case "add-student":
                    db.enrollStudent(args);
                    break;
                case "show-students":
                    db.getStudents(args);
                    break;
                // --- Grade management ---
                case "grade":
                    db.assignGrade(args);
                    break;
                case "student-grades":
                    db.getGradesForStudent(args)
                    break;
                case "gradebook":
                    db.getCurrentClassGrades(args);
                    break;

                // Utilities ---
                case "help":
                    printHelp();
                    break;
                case "quit":
                case "exit":
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Unknown command: '" + cmd
                            + "'. Type 'help' for a list of commands.");
            }
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Bad arguments: " + e.getMessage());
        }
//        catch (SQLException e) {
//            System.out.println("Database error: " + e.getMessage());
//        }
    }

    public void run() throws SQLException, ClassNotFoundException {
        this.db = new Database();
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (!line.isEmpty()) {
                List<String> tokens = tokenize(line);
                dispatch(tokens);
            }

            System.out.print("> ");
        }
    }

    private void printHelp() {
        System.out.println("""
                Available commands:
                
                Class Management:
                  new-class <course> <term> <section> <description>
                  list-classes
                  select-class <course> [term] [section]
                  show-class
                
                Category & Assignment Management:
                  show-categories
                  add-category <name> <weight>
                  show-assignments
                  add-assignment <name> <category> <description> <points>
                
                Student Management:
                  add-student <username> [studentid] [last] [first]
                  show-students [search string]
                
                Grading:
                  grade <assignment> <username> <grade>
                  student-grades <username>
                  gradebook
                
                Other:
                  help
                  quit / exit
                """);
    }


    public static void main(String[] args) {
        try {
            GradeManager gradeManager = new GradeManager();
            gradeManager.run();

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found. "
                    + "Make sure mysql-connector-java is on your classpath.");
        } catch (SQLException e) {
            System.err.println("Could not connect to database: " + e.getMessage());
        }
    }
}
