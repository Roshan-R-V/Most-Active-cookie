package com.example.cookie;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class Main {

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Arguments required: -f <file> -d <YYYY-MM-DD>");
            System.exit(2);
        }

        String fileArg = null;
        String dateArg = null;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("-f".equals(a) || "--file".equals(a)) {
                if (i + 1 < args.length) {
                    fileArg = args[++i];
                } else {
                    System.err.println("Missing value for -f/--file");
                    System.exit(2);
                }
            } else if ("-d".equals(a) || "--date".equals(a)) {
                if (i + 1 < args.length) {
                    dateArg = args[++i];
                } else {
                    System.err.println("Missing value for -d/--date");
                    System.exit(2);
                }
            } else {
                System.err.printf("Unknown argument: %s%n", a);
                System.err.println("Usage: -f <file> -d <YYYY-MM-DD>");
                System.exit(2);
            }
        }

        if (fileArg == null || dateArg == null) {
            System.err.println("Both -f <file> and -d <YYYY-MM-DD> are required.");
            System.exit(2);
        }

        Path path = Path.of(fileArg);
        if (!Files.exists(path)) {
            System.err.printf("File does not exist: %s%n", fileArg);
            System.exit(2);
        }

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(dateArg);
        } catch (DateTimeParseException ex) {
            System.err.printf("Invalid date format. Expected YYYY-MM-DD, got: %s%n", dateArg);
            System.exit(2);
            return;
        }

        try {
            List<String> winners = CookieCounter.mostActiveCookies(path, targetDate);
            for (String cookie : winners) {
                System.out.println(cookie);
            }
            // If none found: print nothing (exit 0)
        } catch (Exception ex) {
            System.err.printf("Error processing file: %s%n", ex.getMessage());
            System.exit(1);
        }
    }
}
