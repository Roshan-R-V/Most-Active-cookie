package com.example.cookie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CookieCounterTest {

    private static final String SAMPLE = String.join("\n",
            "cookie,timestamp",
            "AtY0laUfhglK3lC7,2018-12-09T14:19:00+00:00",
            "SAZuXPGUrfbcn5UA,2018-12-09T10:13:00+00:00",
            "5UAVanZf6UtGyKVS,2018-12-09T07:25:00+00:00",
            "AtY0laUfhglK3lC7,2018-12-09T06:19:00+00:00",
            "SAZuXPGUrfbcn5UA,2018-12-08T22:03:00+00:00",
            "4sMM2LxV07bPJzwf,2018-12-08T21:30:00+00:00",
            "fbcn5UAVanZf6UtG,2018-12-08T09:30:00+00:00",
            "4sMM2LxV07bPJzwf,2018-12-07T23:30:00+00:00");

    @Test
    void mostActiveSingleWinner(@TempDir Path tmp) throws IOException {
        Path p = tmp.resolve("cookies.csv");
        Files.writeString(p, SAMPLE);
        List<String> res = CookieCounter.mostActiveCookies(p, LocalDate.of(2018, 12, 9));
        assertEquals(1, res.size());
        assertEquals("AtY0laUfhglK3lC7", res.get(0));
    }

    @Test
    void mostActiveTie(@TempDir Path tmp) throws IOException {
        String data = String.join("\n",
                "A,2018-12-01T10:00:00+00:00",
                "B,2018-12-01T09:00:00+00:00",
                "A,2018-12-01T08:00:00+00:00",
                "B,2018-12-01T07:00:00+00:00",
                "C,2018-11-30T23:59:59+00:00");
        Path p = tmp.resolve("tie.csv");
        Files.writeString(p, data);
        List<String> res = CookieCounter.mostActiveCookies(p, LocalDate.of(2018, 12, 1));
        assertEquals(2, res.size());
        assertTrue(res.contains("A"));
        assertTrue(res.contains("B"));
    }

    @Test
    void noMatches(@TempDir Path tmp) throws IOException {
        Path p = tmp.resolve("cookies.csv");
        Files.writeString(p, SAMPLE);
        List<String> res = CookieCounter.mostActiveCookies(p, LocalDate.of(2018, 12, 10));
        assertTrue(res.isEmpty());
    }
}
