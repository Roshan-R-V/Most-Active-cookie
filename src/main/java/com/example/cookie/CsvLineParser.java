package com.example.cookie;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Parses a single CSV line of form: cookie,timestamp
 * where timestamp is ISO-8601 like: 2018-12-09T14:19:00+00:00
 *
 * Returns Optional.empty() on malformed or blank lines.
 */
public final class CsvLineParser {

    private CsvLineParser() {
    }

    public static Optional<CookieRecord> parse(String line) {
        if (line == null)
            return Optional.empty();
        String trimmed = line.trim();
        if (trimmed.isEmpty())
            return Optional.empty();

        // split only once
        int commaIndex = trimmed.indexOf(',');
        if (commaIndex <= 0 || commaIndex == trimmed.length() - 1) {
            // either no comma or missing cookie/timestamp
            return Optional.empty();
        }

        String cookie = trimmed.substring(0, commaIndex).trim();
        String timestamp = trimmed.substring(commaIndex + 1).trim();

        if (cookie.isEmpty() || timestamp.length() < 10)
            return Optional.empty();

        // Extract date part YYYY-MM-DD (first 10 chars). This is fast and supports
        // provided format.
        String datePart = timestamp.length() >= 10 ? timestamp.substring(0, 10) : null;
        if (datePart == null)
            return Optional.empty();

        try {
            LocalDate date = LocalDate.parse(datePart); // throws DateTimeException if not valid
            return Optional.of(new CookieRecord(cookie, date));
        } catch (DateTimeException ex) {
            return Optional.empty();
        }
    }

    public static final class CookieRecord {
        private final String cookie;
        private final LocalDate date;

        public CookieRecord(String cookie, LocalDate date) {
            this.cookie = cookie;
            this.date = date;
        }

        public String cookie() {
            return cookie;
        }

        public LocalDate date() {
            return date;
        }
    }
}
