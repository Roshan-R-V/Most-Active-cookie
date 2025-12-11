package com.example.cookie;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulates logic to find most active cookies for a target date.
 */
public final class CookieCounter {

    private static final Logger LOGGER = Logger.getLogger(CookieCounter.class.getName());

    private CookieCounter() {
    }

    /**
     * Scans the file and returns the cookie(s) with the highest occurrence count
     * on the targetDate. Returns empty list if none found.
     *
     * Assumption: file sorted with most recent timestamp on top; therefore once we
     * encounter a date < targetDate we can stop scanning.
     *
     * Malformed lines are skipped quietly.
     */
    public static List<String> mostActiveCookies(Path file, LocalDate targetDate) throws IOException {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(targetDate, "targetDate must not be null");

        Map<String, Integer> counts = new HashMap<>();

        int linesRead = 0;
        int matchedLines = 0;

        try (BufferedReader br = Files.newBufferedReader(file)) {
            LOGGER.log(Level.FINE, "Scanning file {0} for date {1}", new Object[] { file, targetDate });
            String line;
            while ((line = br.readLine()) != null) {
                linesRead++;
                var parsed = CsvLineParser.parse(line);
                if (parsed.isEmpty()) {
                    // skip malformed/header lines
                    continue;
                }
                var rec = parsed.get();
                LocalDate lineDate = rec.date();
                if (lineDate.isEqual(targetDate)) {
                    counts.merge(rec.cookie(), 1, Integer::sum);
                    matchedLines++;
                    continue;
                }
                // If file sorted most-recent-first and we find a date before target -> stop
                if (lineDate.isBefore(targetDate)) {
                    LOGGER.log(Level.FINE, "Encountered earlier date {0} at line {1}; stopping scan (early stop)",
                            new Object[] { lineDate, linesRead });
                    break;
                }
                // else lineDate is after targetDate -> continue scanning
            }
        }

        LOGGER.log(Level.FINE, "Finished scanning. linesRead={0}, matchedLines={1}, uniqueCookies={2}",
                new Object[] { linesRead, matchedLines, counts.size() });

        if (counts.isEmpty())
            return Collections.emptyList();

        // find max count
        int max = counts.values().stream().max(Integer::compareTo).orElse(0);
        List<String> result = new ArrayList<>();
        for (var e : counts.entrySet()) {
            if (e.getValue() == max)
                result.add(e.getKey());
        }
        Collections.sort(result);
        return result;
    }
}
