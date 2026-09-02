package com.codingplatform.service.execution;

import com.codingplatform.entity.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests exercise the execution engine in "local" mode (no Docker required) so they
 * can run in any CI environment with a JDK + python3 on PATH. Run with
 * -Dexecution.tests.enabled=true once those tools are available, e.g.:
 *   mvn test -Dexecution.tests.enabled=true
 */
@SpringBootTest
@TestPropertySource(properties = {"app.execution.mode=local"})
class CodeExecutionServiceTest {

    @Autowired
    private CodeExecutionService executionService;

    private Path workDir;

    @BeforeEach
    void setUp() throws Exception {
        workDir = executionService.createWorkDir();
    }

    @Test
    @EnabledIfSystemProperty(named = "execution.tests.enabled", matches = "true")
    void validPythonCodeProducesCorrectOutput() throws Exception {
        String source = "n = int(input())\nprint(n * 2)\n";
        executionService.writeSource(workDir, Language.PYTHON, source);
        CompileResult compile = executionService.compile(workDir, Language.PYTHON);
        assertTrue(compile.isSuccess());

        ExecutionResult result = executionService.run(workDir, Language.PYTHON, "21\n", 5000, 256);
        assertFalse(result.isTimedOut());
        assertEquals(0, result.getExitCode());
        assertTrue(OutputComparator.matches(result.getStdout(), "42"));
    }

    @Test
    @EnabledIfSystemProperty(named = "execution.tests.enabled", matches = "true")
    void infiniteLoopIsTerminatedByTimeout() throws Exception {
        String source = "while True:\n    pass\n";
        executionService.writeSource(workDir, Language.PYTHON, source);
        executionService.compile(workDir, Language.PYTHON);

        ExecutionResult result = executionService.run(workDir, Language.PYTHON, "", 1000, 256);
        assertTrue(result.isTimedOut());
    }

    @Test
    @EnabledIfSystemProperty(named = "execution.tests.enabled", matches = "true")
    void syntaxErrorIsReportedAsCompilationFailure() throws Exception {
        String source = "public class Main { public static void main(String[] a) { int x = ; } }";
        executionService.writeSource(workDir, Language.JAVA, source);
        CompileResult compile = executionService.compile(workDir, Language.JAVA);
        assertFalse(compile.isSuccess());
        assertNotNull(compile.getErrorOutput());
    }

    @Test
    void outputComparatorIgnoresTrailingWhitespaceAndBlankLines() {
        assertTrue(OutputComparator.matches("42\n", "42"));
        assertTrue(OutputComparator.matches("hello world  \n\n", "hello world"));
        assertFalse(OutputComparator.matches("42", "43"));
    }
}
