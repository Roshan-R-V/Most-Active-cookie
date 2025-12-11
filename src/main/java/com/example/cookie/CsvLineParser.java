package com.example.cookie;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses a single CSV line of form: cookie,timestamp
 * where timestamp is ISO-8601 like: 2018-12-09T14:19:00+00:00
 *
 * Returns Optional.empty() on malformed or blank lines.
 */
public final class CsvLineParser {

    private static final Logger LOGGER = Logger.getLogger(CsvLineParser.class.getName());

    private CsvLineParser() {
    }

    public static Optional<CookieRecord> parse(String line) {
        if (line == null) {
            LOGGER.finer("Received null line to parse");
            return Optional.empty();
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            LOGGER.finest("Skipping empty line");
            return Optional.empty();
        }

        // split only once
        int commaIndex = trimmed.indexOf(',');
        if (commaIndex <= 0 || commaIndex == trimmed.length() - 1) {
            // either no comma or missing cookie/timestamp
            LOGGER.log(Level.FINEST, "Malformed CSV (missing comma or fields): {0}", trimmed);
            return Optional.empty();
        }

        String cookie = trimmed.substring(0, commaIndex).trim();
        String timestamp = trimmed.substring(commaIndex + 1).trim();

        if (cookie.isEmpty() || timestamp.length() < 10) {
            LOGGER.log(Level.FINEST, "Malformed CSV (empty cookie or short timestamp): {0}", trimmed);
            return Optional.empty();
        }

        // Extract date part YYYY-MM-DD (first 10 chars). This is fast and supports
        // provided format.
        String datePart = timestamp.length() >= 10 ? timestamp.substring(0, 10) : null;
        if (datePart == null) {
            LOGGER.finest("Timestamp too short to extract date: " + trimmed);
            return Optional.empty();
        }

        try {
            LocalDate date = LocalDate.parse(datePart); // throws DateTimeException if not valid
            return Optional.of(new CookieRecord(cookie, date));
        } catch (DateTimeException ex) {
            LOGGER.log(Level.FINEST, "Invalid date in CSV line: {0}", trimmed);
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
