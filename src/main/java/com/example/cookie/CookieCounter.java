package com.example.cookie;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Encapsulates logic to find most active cookies for a target date.
 */
public final class CookieCounter {

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

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                var parsed = CsvLineParser.parse(line);
                if (parsed.isEmpty()) {
                    // skip malformed/header lines
                    continue;
                }
                var rec = parsed.get();
                LocalDate lineDate = rec.date();
                if (lineDate.isEqual(targetDate)) {
                    counts.merge(rec.cookie(), 1, Integer::sum);
                    continue;
                }
                // If file sorted most-recent-first and we find a date before target -> stop
                if (lineDate.isBefore(targetDate)) {
                    break;
                }
                // else lineDate is after targetDate -> continue scanning
            }
        }

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
