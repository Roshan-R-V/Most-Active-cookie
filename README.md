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
- Early-stop optimization when the input is sorted most-recent-first: scanning stops once entries are earlier than the requested day.
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
│           └─ CsvLineParserTest.java
```

## Build

From the project root run (Windows cmd.exe):

```cmd
mvn -f d:\Git\Most_Active_Cookie\cookie\pom.xml -q clean package
```

This compiles the code and runs the unit tests. If you prefer full output remove `-q`.

## Run (examples)

Using the compiled classes on the classpath (no shaded jar):

```cmd
java -cp target/most-active-cookie-1.0.0.jar com.example.cookie.Main -f sample/cookie_log.csv -d 2018-12-09
```

Build a shaded (fat) jar and run it:

```cmd
mvn -f d:\Git\Most_Active_Cookie\cookie\pom.xml package
java -jar d:\Git\Most_Active_Cookie\cookie\target\most-active-cookie-1.0.0-shaded.jar -f sample/cookie_log.csv -d 2018-12-09
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

Unit tests use JUnit 5. To run tests:

```cmd
mvn -f d:\Git\Most_Active_Cookie\cookie\pom.xml test
```

What the tests cover

- `CsvLineParserTest` verifies valid parsing, and that header or malformed lines return empty.
- `CookieCounterTest` verifies single-winner behavior, tie behavior, and the no-match case. Tests are designed using temporary files and representative CSV snippets.

## Performance & scaling

- Memory: O(U) where U is the number of unique cookies on the requested date (counts map). The file is streamed; the entire file is not loaded into memory.
- Time: O(N_target) under the sorted-file assumption, where N_target is number of lines from file start until the first line earlier than the requested date. If the file is not sorted, the method still works but scans the full file (O(N)).

## Robustness & edge cases

- Header and malformed lines are ignored (not fatal).
- If no cookies exist for requested date, program exits normally with no stdout.
- Date parsing for the CLI argument is strict: use `YYYY-MM-DD`. The parser inside the CSV extracts the date substring and parses it with `LocalDate` for correctness.

## Extending the project

- Add logging (java.util.logging or SLF4J) for audit/debug.
- Support additional timestamp formats by enhancing `CsvLineParser` to use `OffsetDateTime.parse` or a set of `DateTimeFormatter`s.
- Add an integration test that runs the shaded JAR end-to-end.

## Contact & license

This example is unlicensed intentionally; add your preferred license if you intend to redistribute.

If you'd like, I can also:

- Build the shaded jar and attach the path to it.
- Add a GitHub Actions YAML to run tests and produce the shaded artifact on push.
