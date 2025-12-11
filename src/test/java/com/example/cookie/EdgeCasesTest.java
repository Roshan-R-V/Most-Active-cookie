package com.example.cookie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCasesTest {

    @Test
    void emptyFileReturnsNoMatches(@TempDir Path tmp) throws IOException {
        Path p = tmp.resolve("empty.csv");
        Files.writeString(p, "");
        List<String> res = CookieCounter.mostActiveCookies(p, LocalDate.of(2025, 12, 11));
        assertTrue(res.isEmpty(), "Expected no matches for empty file");
    }

    @Test
    void headerOnlyReturnsNoMatches(@TempDir Path tmp) throws IOException {
        Path p = tmp.resolve("header.csv");
        Files.writeString(p, "cookie,timestamp\n");
        List<String> res = CookieCounter.mostActiveCookies(p, LocalDate.of(2025, 12, 11));
        assertTrue(res.isEmpty(), "Expected no matches for header-only file");
    }

    @Test
    void malformedLinesAndWhitespaceAreSkipped(@TempDir Path tmp) throws IOException {
        String data = String.join("\n",
                "cookie,timestamp",
                "   ",
                "   cookieA  , 2025-12-11T01:00:00+00:00  ",
                "no-comma-here",
                "cookieB,not-a-date",
                "cookieA,2025-12-11T02:00:00+00:00");
        Path p = tmp.resolve("mixed.csv");
        Files.writeString(p, data);
        List<String> res = CookieCounter.mostActiveCookies(p, LocalDate.of(2025, 12, 11));
        assertEquals(1, res.size());
        assertEquals("cookieA", res.get(0));
    }

    @Test
    void unsortedFileLeadsToEarlyBreakBehavior(@TempDir Path tmp) throws IOException {
        // File contains a target-date line, then a before-target line (causes
        // early-break),
        // then another target-date line that should be ignored if early-stop triggers.
        String data = String.join("\n",
                "A,2025-12-12T10:00:00+00:00",
                "B,2025-12-11T09:00:00+00:00",
                "X,2025-12-10T23:00:00+00:00",
                "B,2025-12-11T08:00:00+00:00");
        Path p = tmp.resolve("unsorted.csv");
        Files.writeString(p, data);
        // Because the implementation breaks when it sees a date before target, the
        // second B will be missed.
        List<String> res = CookieCounter.mostActiveCookies(p, LocalDate.of(2025, 12, 11));
        assertEquals(1, res.size());
        assertEquals("B", res.get(0));
    }

    @Test
    void shortTimestampIsSkipped(@TempDir Path tmp) throws IOException {
        String data = String.join("\n",
                "cookie,timestamp",
                "A,2018-12",
                "B,2018-12-11T10:00:00+00:00");
        Path p = tmp.resolve("shortts.csv");
        Files.writeString(p, data);
        List<String> res = CookieCounter.mostActiveCookies(p, LocalDate.of(2018, 12, 11));
        assertEquals(1, res.size());
        assertEquals("B", res.get(0));
    }
}
