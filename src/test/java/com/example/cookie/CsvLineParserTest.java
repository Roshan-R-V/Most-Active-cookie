package com.example.cookie;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CsvLineParserTest {

    @Test
    void parseValidLine() {
        var opt = CsvLineParser.parse("cookie123,2018-12-09T14:19:00+00:00");
        assertTrue(opt.isPresent());
        var rec = opt.get();
        assertEquals("cookie123", rec.cookie());
        assertEquals(LocalDate.of(2018, 12, 9), rec.date());
    }

    @Test
    void parseHeaderOrMalformedReturnsEmpty() {
        assertTrue(CsvLineParser.parse("cookie,timestamp").isEmpty());
        assertTrue(CsvLineParser.parse("").isEmpty());
        assertTrue(CsvLineParser.parse("no-comma-here").isEmpty());
        assertTrue(CsvLineParser.parse("cookie,not-a-date").isEmpty());
    }
}
