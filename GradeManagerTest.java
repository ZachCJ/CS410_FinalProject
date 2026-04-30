import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GradeManager parsing logic
 *   - ParsingTests : no DB needed, tests tokenizer + arg validation
 */
public class GradeManagerTest {

    // =========================================================================
    // Parsing Tests — no database required
    // =========================================================================
    @Nested
    @DisplayName("Tokenizer Tests")
    class TokenizerTests {

        @Test
        @DisplayName("Basic space-separated tokens")
        void basicTokens() {
            List<String> tokens = GradeManager.tokenize("new-class CS410 Sp20 1");
            assertEquals(List.of("new-class", "CS410", "Sp20", "1"), tokens);
        }

        @Test
        @DisplayName("Quoted string is treated as single token")
        void quotedString() {
            List<String> tokens = GradeManager.tokenize("new-class CS410 Sp20 1 \"Databases Fall 2020\"");
            assertEquals(5, tokens.size());
            assertEquals("Databases Fall 2020", tokens.get(4));
        }

        @Test
        @DisplayName("Multiple quoted strings")
        void multipleQuotedStrings() {
            List<String> tokens = GradeManager.tokenize("add-assignment HW1 Homework \"First homework\" 100");
            assertEquals(List.of("add-assignment", "HW1", "Homework", "First homework", "100"), tokens);
        }

        @Test
        @DisplayName("Empty input returns empty list")
        void emptyInput() {
            List<String> tokens = GradeManager.tokenize("");
            assertTrue(tokens.isEmpty());
        }

        @Test
        @DisplayName("Extra spaces between tokens are ignored")
        void extraSpaces() {
            List<String> tokens = GradeManager.tokenize("list-classes");
            assertEquals(List.of("list-classes"), tokens);
        }

        @Test
        @DisplayName("Unquoted multi-word description joined with subList")
        void unquotedMultiWord() {
            List<String> tokens = GradeManager.tokenize("new-class CS410 Sp20 1 Databases Fall 2020");
            // indices: 0=new-class, 1=CS410, 2=Sp20, 3=1, 4=Databases, 5=Fall, 6=2020
            assertEquals(7, tokens.size());

            // description starts at index 4 (everything after course, term, section)
            String desc = String.join(" ", tokens.subList(4, tokens.size()));
            assertEquals("Databases Fall 2020", desc);
        }
    }

    // =========================================================================
    @Nested
    @DisplayName("Dispatch / Arg Validation Tests")
    class DispatchTests {

        private GradeManager gm;
        private ByteArrayOutputStream out;

        @BeforeEach
        void setup() throws SQLException, ClassNotFoundException {
            gm  = new GradeManager();
            gm.db = new Database(); // stubbed constructor — no real connection
            out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));
        }

        @AfterEach
        void teardown() {
            System.setOut(System.out);
        }

        // --- requireActiveClass ---

        @Test
        @DisplayName("Commands requiring active class fail with no class selected")
        void requireActiveClassFails() {
            gm.activeClassId = null;
            gm.dispatch(GradeManager.tokenize("show-class"));
            assertTrue(out.toString().contains("No class selected"));
        }

        // --- new-class ---

        @Test
        @DisplayName("new-class with too few args shows usage error")
        void newClassTooFewArgs() {
            gm.dispatch(GradeManager.tokenize("new-class CS410"));
            assertTrue(out.toString().contains("Usage:"));
        }

        @Test
        @DisplayName("new-class with non-integer section shows error")
        void newClassBadSection() {
            gm.dispatch(GradeManager.tokenize("new-class CS410 Sp20 abc Databases"));
            assertTrue(out.toString().contains("must be a whole number"));
        }

        // --- select-class ---

        @Test
        @DisplayName("select-class with 4 args shows usage error")
        void selectClassTooManyArgs() {
            gm.dispatch(GradeManager.tokenize("select-class CS410 Sp20 1 extra"));
            assertTrue(out.toString().contains("Usage:"));
        }

        // --- add-category ---

        @Test
        @DisplayName("add-category with non-number weight shows error")
        void addCategoryBadWeight() {
            gm.activeClassId = 1;
            gm.dispatch(GradeManager.tokenize("add-category Homework abc"));
            assertTrue(out.toString().contains("must be a number"));
        }

        // --- add-student ---

        @Test
        @DisplayName("add-student with 2 or 3 args shows error")
        void addStudentBadArgCount() {
            gm.activeClassId = 1;
            gm.dispatch(GradeManager.tokenize("add-student jsmith 123456"));
            assertTrue(out.toString().contains("add-student takes 1 arg"));
        }

        // --- grade ---

        @Test
        @DisplayName("grade with non-number grade shows error")
        void gradeBadGrade() {
            gm.activeClassId = 1;
            gm.dispatch(GradeManager.tokenize("grade HW1 jsmith abc"));
            assertTrue(out.toString().contains("must be a number"));
        }

        // --- unknown command ---

        @Test
        @DisplayName("Unknown command shows helpful message")
        void unknownCommand() {
            gm.dispatch(GradeManager.tokenize("foobar"));
            assertTrue(out.toString().contains("Unknown command"));
            assertTrue(out.toString().contains("help"));
        }

        // --- help ---

        @Test
        @DisplayName("help prints all command categories")
        void helpOutput() {
            gm.dispatch(GradeManager.tokenize("help"));
            String output = out.toString();
            assertTrue(output.contains("Class Management"));
            assertTrue(output.contains("Student Management"));
            assertTrue(output.contains("Grading"));
        }
    }
}