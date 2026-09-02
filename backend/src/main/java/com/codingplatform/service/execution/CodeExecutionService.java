package com.codingplatform.service.execution;

import com.codingplatform.entity.Language;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Sandboxed code execution engine.
 *
 * mode = "docker": every compile/run happens inside a short-lived, network-disabled,
 *   resource-capped Docker container (openjdk / python / gcc / node images), matching
 *   the isolation model described in the project report.
 *
 * mode = "local": compiles/runs directly with the host toolchain (javac/java, python3,
 *   g++, node). This is for local development on machines without Docker installed and
 *   provides NO process isolation — do not use in a real deployment that accepts
 *   submissions from untrusted users.
 */
@Slf4j
@Service
public class CodeExecutionService {

    @Value("${app.execution.mode:local}")
    private String mode;

    @Value("${app.execution.workdir:./exec-tmp}")
    private String workdirBase;

    @Value("${app.execution.timeout-seconds:5}")
    private int defaultTimeoutSeconds;

    @Value("${app.execution.memory-limit-mb:512}")
    private int defaultMemoryLimitMb;

    private static final int COMPILE_TIMEOUT_SECONDS = 15;

    /** Creates an isolated working directory for one submission's compile+run lifecycle. */
    public Path createWorkDir() throws IOException {
        Path base = Path.of(workdirBase);
        Files.createDirectories(base);
        Path dir = base.resolve(UUID.randomUUID().toString());
        Files.createDirectories(dir);
        return dir;
    }

    public void cleanupWorkDir(Path dir) {
        try {
            if (dir == null || !Files.exists(dir)) return;
            Files.walk(dir)
                    .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) { }
                    });
        } catch (IOException e) {
            log.warn("Failed to clean up execution workdir {}: {}", dir, e.getMessage());
        }
    }

    /** Writes the submitted source to the correct filename for the language's toolchain. */
    public void writeSource(Path workDir, Language language, String sourceCode) throws IOException {
        String filename = switch (language) {
            case JAVA -> "Main.java";
            case PYTHON -> "solution.py";
            case CPP -> "solution.cpp";
            case JAVASCRIPT -> "solution.js";
        };
        Files.writeString(workDir.resolve(filename), sourceCode, StandardCharsets.UTF_8);
    }

    /** Compiles the source (no-op for interpreted languages). */
    public CompileResult compile(Path workDir, Language language) throws IOException, InterruptedException {
        List<String> compileCmd = buildCompileCommand(workDir, language);
        if (compileCmd == null) {
            return new CompileResult(true, null); // interpreted language, nothing to compile
        }

        Process process = new ProcessBuilder(compileCmd)
                .directory(workDir.toFile())
                .start();

        StreamGobbler err = new StreamGobbler(process.getErrorStream());
        StreamGobbler out = new StreamGobbler(process.getInputStream());
        err.start();
        out.start();

        boolean finished = process.waitFor(COMPILE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return new CompileResult(false, "Compilation timed out after " + COMPILE_TIMEOUT_SECONDS + "s");
        }
        err.join(2000);
        out.join(2000);

        if (process.exitValue() != 0) {
            String errorOutput = err.getOutput();
            return new CompileResult(false, errorOutput.isBlank() ? out.getOutput() : errorOutput);
        }
        return new CompileResult(true, null);
    }

    /** Runs the compiled/interpreted program against a single stdin input with resource limits. */
    public ExecutionResult run(Path workDir, Language language, String stdin,
                                Integer timeLimitMsOverride, Integer memoryLimitMbOverride) throws IOException, InterruptedException {

        int timeoutMs = timeLimitMsOverride != null ? timeLimitMsOverride : defaultTimeoutSeconds * 1000;
        int memoryLimitMb = memoryLimitMbOverride != null ? memoryLimitMbOverride : defaultMemoryLimitMb;

        List<String> runCmd = buildRunCommand(workDir, language, memoryLimitMb);

        Process process = new ProcessBuilder(runCmd)
                .directory(workDir.toFile())
                .start();

        // Feed stdin then close, so the program sees EOF.
        try (var stdinStream = process.getOutputStream()) {
            if (stdin != null && !stdin.isEmpty()) {
                stdinStream.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
            stdinStream.flush();
        } catch (IOException ignored) {
            // program may not read stdin at all and could close the pipe early
        }

        StreamGobbler stdoutGobbler = new StreamGobbler(process.getInputStream());
        StreamGobbler stderrGobbler = new StreamGobbler(process.getErrorStream());
        stdoutGobbler.start();
        stderrGobbler.start();

        long start = System.currentTimeMillis();
        boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        long elapsed = System.currentTimeMillis() - start;

        boolean timedOut = !finished;
        if (timedOut) {
            process.destroyForcibly();
        }

        stdoutGobbler.join(2000);
        stderrGobbler.join(2000);

        int exitCode = timedOut ? -1 : process.exitValue();

        return new ExecutionResult(
                stdoutGobbler.getOutput(),
                stderrGobbler.getOutput(),
                exitCode,
                timedOut,
                elapsed,
                0L // real memory measurement requires docker stats / cgroups; see README
        );
    }

    // ---------------------------------------------------------------------
    // Command builders
    // ---------------------------------------------------------------------

    private List<String> buildCompileCommand(Path workDir, Language language) {
        if ("docker".equalsIgnoreCase(mode)) {
            return switch (language) {
                case JAVA -> dockerCmd(workDir, "openjdk:17-slim", "javac Main.java");
                case CPP -> dockerCmd(workDir, "gcc:latest", "g++ -O2 -o solution solution.cpp");
                case PYTHON, JAVASCRIPT -> null;
            };
        }
        // local mode
        return switch (language) {
            case JAVA -> List.of("javac", "Main.java");
            case CPP -> List.of("g++", "-O2", "-o", "solution", "solution.cpp");
            case PYTHON, JAVASCRIPT -> null;
        };
    }

    private List<String> buildRunCommand(Path workDir, Language language, int memoryLimitMb) {
        if ("docker".equalsIgnoreCase(mode)) {
            return switch (language) {
                case JAVA -> dockerRunCmd(workDir, "openjdk:17-slim", "java -Xmx" + memoryLimitMb + "m Main", memoryLimitMb);
                case PYTHON -> dockerRunCmd(workDir, "python:3.11-slim", "python3 solution.py", memoryLimitMb);
                case CPP -> dockerRunCmd(workDir, "gcc:latest", "./solution", memoryLimitMb);
                case JAVASCRIPT -> dockerRunCmd(workDir, "node:20-slim", "node solution.js", memoryLimitMb);
            };
        }
        // local mode
        return switch (language) {
            case JAVA -> List.of("java", "-Xmx" + memoryLimitMb + "m", "Main");
            case PYTHON -> List.of("python3", "solution.py");
            case CPP -> List.of("./solution");
            case JAVASCRIPT -> List.of("node", "solution.js");
        };
    }

    /** One-shot docker container for a compile step, sharing the host workDir via bind mount. */
    private List<String> dockerCmd(Path workDir, String image, String shellCommand) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-v");
        cmd.add(workDir.toAbsolutePath() + ":/box");
        cmd.add("-w");
        cmd.add("/box");
        cmd.add("--network=none");
        cmd.add("--pids-limit=128");
        cmd.add(image);
        cmd.add("sh");
        cmd.add("-c");
        cmd.add(shellCommand);
        return cmd;
    }

    /** Same as dockerCmd but with -i (stdin piping) and the memory/CPU limits applied to the run step. */
    private List<String> dockerRunCmd(Path workDir, String image, String shellCommand, int memoryLimitMb) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-i");
        cmd.add("-v");
        cmd.add(workDir.toAbsolutePath() + ":/box");
        cmd.add("-w");
        cmd.add("/box");
        cmd.add("--network=none");
        cmd.add("--memory=" + memoryLimitMb + "m");
        cmd.add("--memory-swap=" + memoryLimitMb + "m");
        cmd.add("--cpus=1");
        cmd.add("--pids-limit=128");
        cmd.add(image);
        cmd.add("sh");
        cmd.add("-c");
        cmd.add(shellCommand);
        return cmd;
    }
}
