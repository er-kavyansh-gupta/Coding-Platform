package com.codingplatform.service.execution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {
    private String stdout;
    private String stderr;
    private int exitCode;
    private boolean timedOut;
    private long executionTimeMs;
    private long memoryKb; // 0 if not measurable in current mode
}
