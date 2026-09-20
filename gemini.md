# GEMINI.md — Smart Finance Planner

Persistent rules for this project. Full technical spec: `SPEC.md`. Team workflow: `GUIDELINES.md`. Read `SPEC.md` before generating or modifying any code — it is the source of truth for schema, screens, and features; this file is enforcement rules only.

## Tech stack — do not deviate
- Language: Java 17. No Kotlin, no Python, no JavaScript.
- GUI: Swing + FlatLaf (`com.formdev:flatlaf`). Default theme `FlatLightLaf`; dark theme `FlatDarkLaf`. No JavaFX, no Swing default look-and-feel.
- Database: SQLite via `org.xerial:sqlite-jdbc`, accessed only through `PreparedStatement`. No CSV, no flat files, no ORM libraries (Hibernate, JPA) — plain JDBC in DAO classes only.
- Build: Maven (`pom.xml`). No Gradle.
- Testing: JUnit 5.

## Architecture — enforce strictly
- Layers: `gui` → `engine` → `data` → SQLite. Never let a `gui` class import `java.sql.*` or open a `Connection` directly — all DB access goes through a DAO in `data/`.
- `engine` classes (`InflationCalculator`, `SavingsPlanner`, `ProgressTracker`) must have zero Swing imports and zero JDBC imports — pure logic, unit-testable standalone.
- `model` classes are plain data holders (fields + getters/setters + minimal derived fields like `getYearsLeft()`). No business logic and no persistence logic inside model classes.
- Package structure is fixed: `com.sfp.model`, `com.sfp.engine`, `com.sfp.data`, `com.sfp.gui`, `com.sfp.gui.components`, `com.sfp.util`. Do not invent new top-level packages without updating `SPEC.md` first.

## Database rules
- Schema is defined in `SPEC.md` §4 — do not alter table/column names without updating that file too.
- All writes wrapped in try/catch; failures surface as a user-facing dialog, never a silent console stack trace.
- Every DB-touching method must have a corresponding DAO test using `jdbc:sqlite::memory:`, not the real data file.
- Foreign keys and `ON DELETE CASCADE` must be honored — enable foreign key support explicitly (`PRAGMA foreign_keys = ON;`) on every connection, since SQLite disables it by default.

## Currency rules
- Currency is **display-only** — never call an external API or perform exchange-rate conversion.
- All monetary formatting goes through one shared `util.CurrencyFormatter` — never inline `String.format("₹...")` or any other literal currency symbol anywhere in `gui/`.
- Every `Goal` carries its own `currencyCode`; never assume a single global currency.

## Validation rules
- All input validation goes through `util.Validator`, shared identically by GUI code and JUnit tests. No duplicate validation logic inside GUI classes.
- Validate before any calculation and before any DB write — never let unvalidated input reach `engine` or `data`.
- Guard every division (years = 0, income = 0, futureCost = 0) — a caught bug here is a runtime crash for the user, not just a wrong number.

## UI rules
- Follow the color tokens, typography, and spacing rules in `SPEC.md` §10 exactly — do not introduce ad hoc colors or fonts.
- Every screen listed in `SPEC.md` §11 must exist as its own `gui` class/panel — do not merge screens together or skip any.
- Primary actions = filled navy buttons; secondary/cancel actions = outlined buttons. Keep this distinction consistent across all screens.
- No blocking the Event Dispatch Thread — wrap any slow operation in `SwingWorker`.

## Code style
- Every public method gets a one-line Javadoc comment describing what it does, especially in `engine` and `data`.
- Prefer composition over inheritance for GUI panels; extract repeated UI patterns (goal cards, progress bars) into `gui/components`.
- No magic numbers for validation bounds (e.g., inflation 0–50%) — define them as named constants in `Validator`.
- Keep methods under ~40 lines; extract helpers rather than growing a single method.

## When implementing a feature
1. Check `SPEC.md` for the exact section covering it before writing code.
2. Implement model → DAO → engine → GUI, in that order, matching the build order in `GUIDELINES.md` §3.
3. Write the JUnit test alongside the engine/DAO code, not after.
4. Do not mark a feature complete until it appears in `SPEC.md` §16 (Acceptance Criteria) as satisfied.

## Never do
- Never hardcode a single user, a single currency, or a single goal — the schema supports many of each.
- Never write directly to the real `data/smart_finance_planner.db` from a test.
- Never catch an exception and do nothing with it (no empty catch blocks).
- Never introduce a new dependency not listed in "Tech stack" above without flagging it first.