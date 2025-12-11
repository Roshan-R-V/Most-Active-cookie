# Most Active Cookie (Java)

## Purpose

This is a small, production-oriented CLI tool that finds the most active cookie(s) for a given date from a CSV logfile. The CSV format is expected to be lines of:

```
cookie,timestamp
```

where the timestamp is ISO-8601-like (for example: `2018-12-09T14:19:00+00:00`). The tool prints the cookie(s) with the highest occurrence on the given date, one per line.

## Key features

- Minimal, dependency-free implementation (only JDK and JUnit 5 for tests).
- Memory-efficient streaming read (BufferedReader).
- Early-stop optimization when the input is sorted most-recent-first: scanning stops once entries are earlier than the requested day (this improves performance but requires the file ordering assumption).
- Robust: skips malformed lines and headers instead of failing.
- Deterministic output: ties are sorted alphabetically.

## Prerequisites

- Java 17 (JDK) or newer installed and `java`/`javac` available on PATH.
- Apache Maven 3.6+ available on PATH to build and run tests.

## Project layout

Standard Maven layout used in this repo:

```
most-active-cookie-java/
├─ pom.xml
├─ README.md
├─ sample/
│  └─ cookie_log.csv
├─ src
│  ├─ main
│  │  └─ java
│  │     └─ com.example.cookie
│  │        ├─ Main.java
│  │        ├─ CookieCounter.java
│  │        └─ CsvLineParser.java
│  └─ test
│     └─ java
│        └─ com.example.cookie
│           ├─ CookieCounterTest.java
│           ├─ CsvLineParserTest.java
│           └─ EdgeCasesTest.java
```

## Build

From the project root run (Windows cmd.exe):

```cmd
mvn -q clean package
```

This compiles the code and runs the unit tests. If you prefer full output remove `-q`.

## Run (examples)

Using the compiled classes on the classpath (no shaded jar):

```cmd
java -cp target/most-active-cookie-1.0.0.jar com.example.cookie.Main -f sample/cookie_log.csv -d 2018-12-09
```

Build a shaded (fat) jar and run it:

```cmd
mvn package
java -jar target/most-active-cookie-1.0.0-shaded.jar -f sample/cookie_log.csv -d 2018-12-09
```

## Notes on running

- Required CLI arguments: `-f <file>` and `-d <YYYY-MM-DD>` (UTC date). Both are mandatory.
- Exit codes:
  - `0` success (regular completion; prints winners or nothing if none)
  - `1` runtime/processing error
  - `2` incorrect usage / missing arguments / invalid date format / missing file
- Output: the cookie string(s) printed to stdout, one per line. If multiple cookies tie for most occurrences, each is printed on its own line (sorted alphabetically).

## How it works (short)

1. `Main` parses CLI args, validates file and date.
2. `CookieCounter.mostActiveCookies(Path, LocalDate)` opens the file and reads it line by line.
3. Each line is parsed by `CsvLineParser.parse(String)` which returns an `Optional<CookieRecord>` with the cookie and the parsed `LocalDate` (the parser extracts the first 10 characters of the timestamp and parses that as `YYYY-MM-DD`). Malformed lines return empty and are skipped.
4. If a parsed line date equals the target date, the cookie counter for that cookie is incremented. If a parsed line date is before the target date and the file is sorted most-recent-first, the scan breaks early.
5. After scanning, the highest count is computed and all cookies with that count are returned.

## Testing

Unit tests use JUnit 5. To run tests from the project root:

```cmd
mvn test
```

What the tests cover

- `CsvLineParserTest` verifies valid parsing, and that header or malformed lines return empty.
- `CookieCounterTest` verifies single-winner behavior, tie behavior, and the no-match case. Tests are designed using temporary files and representative CSV snippets.

### Edge case tests

An additional `EdgeCasesTest` is included to cover and document important edge behaviors:

- empty file
- header-only file
- malformed lines, whitespace and short timestamps (these are skipped)
- unsorted inputs that demonstrate the early-stop behavior (the implementation stops scanning when it sees a line with a date earlier than the requested date; that early-stop may skip later matching lines if the file is not sorted most-recent-first)

Run just the edge-case tests:

```cmd
mvn -Dtest=com.example.cookie.EdgeCasesTest test
```

## Logging (how to enable)

The project uses `java.util.logging` with conservative logging levels (FINE/FINEST) for diagnostic messages so normal CLI runs remain quiet. You can enable logging during development or troubleshooting by supplying a logging configuration file.

Create a file `logging.properties` in the project root with the following minimal content to enable FINE logs to the console:

```
handlers= java.util.logging.ConsoleHandler
.level= FINE
java.util.logging.ConsoleHandler.level = FINE
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
```

Then run the jar with the system property pointing at that file:

```cmd
java -Djava.util.logging.config.file=logging.properties -jar target/most-active-cookie-1.0.0-shaded.jar -f sample/cookie_log.csv -d 2018-12-09
```

Alternatively, enable only FINE logs for the application classes by updating the config (example):

```
.level= INFO
com.example.cookie.level = FINE
java.util.logging.ConsoleHandler.level = FINE
```

## Notes

- The logging is intentionally non-intrusive: normal users will not see internal diagnostics unless logging is explicitly enabled.
- The `EdgeCasesTest` demonstrates the current early-stop assumption; if you prefer order-agnostic correctness, consider removing the early-stop `break` in `CookieCounter` (this will scan the entire file).
