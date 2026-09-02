package com.codingplatform.service.execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Reads an InputStream on a background thread so that a process's stdout and stderr
 * can be drained concurrently without risking a deadlock from a full OS pipe buffer.
 */
public class StreamGobbler extends Thread {

    private static final int MAX_CAPTURED_CHARS = 200_000; // guard against runaway output

    private final InputStream inputStream;
    private final StringBuilder buffer = new StringBuilder();

    public StreamGobbler(InputStream inputStream) {
        this.inputStream = inputStream;
        setDaemon(true);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            char[] chunk = new char[4096];
            int read;
            while ((read = reader.read(chunk)) != -1) {
                if (buffer.length() < MAX_CAPTURED_CHARS) {
                    buffer.append(chunk, 0, read);
                }
            }
        } catch (IOException ignored) {
            // stream closed because the process was destroyed — expected on timeout
        }
    }

    public String getOutput() {
        return buffer.toString();
    }
}
