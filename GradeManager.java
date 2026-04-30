import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GradeManager {

    Database db;
    Integer activeClassId = null; // tracks the currently selected class

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
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    // -------------------------------------------------------------------------
    // Dispatcher
    // -------------------------------------------------------------------------
    public void dispatch(List<String> tokens) {
        if (tokens.isEmpty()) return;

        String cmd = tokens.get(0).toLowerCase();
        List<String> args = tokens.subList(1, tokens.size());

        try {
            switch (cmd) {
                case "new-class" -> handleNewClass(args);
                case "list-classes" -> handleListClasses();
                case "select-class" -> handleSelectClass(args);
                case "show-class" -> handleShowClass();
                case "show-categories" -> handleShowCategories();
                case "add-category" -> handleAddCategory(args);
                case "show-assignments" -> handleShowAssignments();
                case "add-assignment" -> handleAddAssignment(args);
                case "add-student" -> handleAddStudent(args);
                case "show-students" -> handleShowStudents(args);
                case "grade" -> handleGrade(args);
                case "student-grades" -> handleStudentGrades(args);
                case "gradebook" -> handleGradebook();
                case "help" -> printHelp();
                case "quit", "exit" -> {
                    System.out.println("Goodbye!");
                    try {
                        db.closeDatabaseConnection();
                    } catch (RuntimeException e) {
                        System.err.println("Error occurred on close.");
                        System.exit(2);
                    }
                    System.exit(0);
                }
                default -> System.out.println("Unknown command: '" + cmd + "'. Type 'help' for a list of commands.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Throws if no class has been selected yet.
     */
    private void requireActiveClass() {
        if (activeClassId == null)
            throw new IllegalArgumentException("No class selected. Use 'select-class' first.");
    }

    /**
     * Throws if the wrong number of args were provided, with a usage hint.
     */
    private void requireArgs(List<String> args, int min, int max, String usage) {
        if (args.size() < min || args.size() > max)
            throw new IllegalArgumentException("Usage: " + usage);
    }

    private void requireArgs(List<String> args, int exact, String usage) {
        requireArgs(args, exact, exact, usage);
    }

    // -------------------------------------------------------------------------
    // Class Management handlers
    // -------------------------------------------------------------------------

    /**
     * new-class <courseNum> <term> <section> <description>
     * e.g. new-class CS410 Sp20 1 "Databases"
     */
    private void handleNewClass(List<String> args) {
        requireArgs(args, 4, "new-class <courseNum> <term> <section> <description>");
        String courseNum = args.get(0);
        String term = args.get(1);
        int section = parseIntArg(args.get(2), "section");
        String description = args.get(3);

        System.out.printf("[new-class] course=%s term=%s section=%d desc=%s%n",
                courseNum, term, section, description);
        db.createClass(courseNum, term, section, description);
    }

    /**
     * list-classes  (no args)
     */
    private void handleListClasses() {
        System.out.println("[list-classes]");
        db.listClassesWithStudents();
    }

    /**
     * select-class <courseNum> [term] [section]
     * - 1 arg: selects the only section in the most recent term (fails if multiple)
     * - 2 args: selects the only section for that course+term (fails if multiple)
     * - 3 args: selects exact course+term+section
     */
    private void handleSelectClass(List<String> args) {
        requireArgs(args, 1, 3, "select-class <courseNum> [term] [section]");
        String courseNum = args.get(0);
        String term      = args.size() >= 2 ? args.get(1) : null;
        Integer section  = args.size() == 3 ? parseIntArg(args.get(2), "section") : null;

        int result = db.selectClass(courseNum, term, section);
        if (result != -1) {
            activeClassId = result;
        }
    }

    /**
     * show-class  (no args)
     */
    private void handleShowClass() {
        requireActiveClass();
        System.out.printf("[show-class] activeClassId=%d%n", activeClassId);
        db.showClass(activeClassId);
    }

    // -------------------------------------------------------------------------
    // Category & Assignment handlers
    // -------------------------------------------------------------------------

    /**
     * show-categories  (no args)
     */
    private void handleShowCategories() {
        requireActiveClass();
        System.out.println("[show-categories]");
        db.showCategories(activeClassId);
    }

    /**
     * add-category <name> <weight>
     * e.g. add-category Homework 40
     */
    private void handleAddCategory(List<String> args) {
        requireActiveClass();
        requireArgs(args, 2, "add-category <name> <weight>");
        String name = args.get(0);
        double weight = parseDoubleArg(args.get(1), "weight");

        System.out.printf("[add-category] name=%s weight=%.2f%n", name, weight * 100);
        db.addCategory(activeClassId, name, weight);
    }

    /**
     * show-assignments  (no args)
     */
    private void handleShowAssignments() {
        requireActiveClass();
        System.out.println("[show-assignments]");
        db.showAssignments(activeClassId);
    }

    /**
     * add-assignment <name> <category> <description> <points>
     * e.g. add-assignment HW1 Homework "First homework" 100
     */
    private void handleAddAssignment(List<String> args) {
        requireActiveClass();
        requireArgs(args, 4, "add-assignment <name> <category> <description> <points>");
        String name = args.get(0);
        String category = args.get(1);
        String description = args.get(2);
        int points = parseIntArg(args.get(3), "points");

        System.out.printf("[add-assignment] name=%s category=%s desc=%s points=%d%n",
                name, category, description, points);
        db.addAssignment(activeClassId, name, category, description, points);
    }

    // -------------------------------------------------------------------------
    // Student Management handlers
    // -------------------------------------------------------------------------

    /**
     * add-student <username> [studentId] [lastName] [firstName]
     * - 1 arg:  enroll an already-existing student by username
     * - 4 args: add a new student and enroll them
     */
    private void handleAddStudent(List<String> args) {
        requireActiveClass();
        requireArgs(args, 1, 4, "add-student <username> | add-student <username> <studentId> <last> <first>");

        String username = args.get(0);

        if (args.size() == 1) {
            // Enroll existing student
            System.out.printf("[add-student] enroll existing: username=%s%n", username);
            db.enrollStudent(activeClassId, username);
        } else if (args.size() == 4) {
            // Add new student and enroll
            String studentId = args.get(1);
            String lastName = args.get(2);
            String firstName = args.get(3);
            System.out.printf("[add-student] new: username=%s id=%s name=%s %s%n",
                    username, studentId, firstName, lastName);
            db.addStudent(activeClassId, username, studentId, lastName, firstName);
        } else {
            throw new IllegalArgumentException(
                    "add-student takes 1 arg (enroll existing) or 4 args (add new). Got " + args.size());
        }
    }

    /**
     * show-students [searchString]
     * - 0 args: show all students in the active class
     * - 1 arg:  show students whose name/username contains the string
     */
    private void handleShowStudents(List<String> args) {
        requireActiveClass();
        requireArgs(args, 0, 1, "show-students [searchString]");

        if (args.isEmpty()) {
            System.out.println("[show-students] all");
            db.getStudents(activeClassId);
        } else {
            String search = args.get(0);
            System.out.printf("[show-students] search='%s'%n", search);
            db.getStudents(activeClassId, search);
        }
    }

    // -------------------------------------------------------------------------
    // Grade handlers
    // -------------------------------------------------------------------------

    /**
     * grade <assignmentName> <username> <grade>
     * e.g. grade HW1 jsmith 95
     */
    private void handleGrade(List<String> args) {
        requireActiveClass();
        requireArgs(args, 3, "grade <assignmentName> <username> <grade>");
        String assignmentName = args.get(0);
        String username = args.get(1);
        double grade = parseDoubleArg(args.get(2), "grade");

        System.out.printf("[grade] assignment=%s username=%s grade=%.2f%n",
                assignmentName, username, grade);
        db.assignGrade(activeClassId, assignmentName, username, grade);
    }

    /**
     * student-grades <username>
     */
    private void handleStudentGrades(List<String> args) {
        requireActiveClass();
        requireArgs(args, 1, "student-grades <username>");
        String username = args.get(0);

        System.out.printf("[student-grades] username=%s%n", username);
        db.getGradesForStudent(activeClassId, username);
    }

    /**
     * gradebook  (no args)
     */
    private void handleGradebook() {
        requireActiveClass();
        System.out.println("[gradebook]");
        db.getCurrentClassGrades(activeClassId);
    }

    // -------------------------------------------------------------------------
    // Parsing utilities
    // -------------------------------------------------------------------------

    private int parseIntArg(String val, String fieldName) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + fieldName + "' must be a whole number, got: " + val);
        }
    }

    private double parseDoubleArg(String val, String fieldName) {
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + fieldName + "' must be a number, got: " + val);
        }
    }

    // -------------------------------------------------------------------------
    // Main loop
    // -------------------------------------------------------------------------
    public void run() throws SQLException, ClassNotFoundException {
        promptConnectionInfo();
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

    /**
     * Prompts the user for database connection info at startup,
     * then initializes the Database connection.
     */
    private void promptConnectionInfo() throws SQLException, ClassNotFoundException {
        Scanner setup = new Scanner(System.in);

        System.out.print("Enter port: ");
        String port = setup.nextLine().trim();

        System.out.print("Enter schema/database name: ");
        String schema = setup.nextLine().trim();

        System.out.print("Enter database password: ");
        String password = setup.nextLine().trim();

        System.out.println("Attempting to connect to " + schema + " on " + ":" + port + "...");
        this.db = new Database(port, password, schema);
        System.out.println("Connection Succeeded!");
        System.out.println("Type help for database commands");
        System.out.println();
    }

    private void printHelp() {
        System.out.println("""
                Available commands:
                
                Class Management:
                  new-class <courseNum> <term> <section> <description>
                        (Note: Enter multi-worded description inside "" i.e. "The Best Class Ever")
                  list-classes
                  select-class <courseNum> [term] [section]
                  show-class
                
                Category & Assignment Management:
                  show-categories
                  add-category <name> <weight>
                        (Note: Enter weight in as a decimal value i.e. 1 = 100%, 0.1 = 10%)
                  show-assignments
                  add-assignment <name> <category> <description> <points>
                
                Student Management:
                  add-student <username> <studentId> <last> <first>
                  add-student <username>
                  show-students [searchString]
                
                Grading:
                  grade <assignmentName> <username> <grade>
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
            System.err.println("Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}