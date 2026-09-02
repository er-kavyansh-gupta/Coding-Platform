package com.codingplatform.service.execution;

public class OutputComparator {

    /** Compares actual vs expected output ignoring trailing whitespace per line and trailing blank lines. */
    public static boolean matches(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String[] lines = s.replace("\r\n", "\n").split("\n", -1);
        StringBuilder sb = new StringBuilder();
        int lastNonEmpty = -1;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].strip().isEmpty()) lastNonEmpty = i;
        }
        for (int i = 0; i <= lastNonEmpty; i++) {
            sb.append(lines[i].stripTrailing());
            if (i < lastNonEmpty) sb.append("\n");
        }
        return sb.toString();
    }
}
