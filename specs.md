# Smart Finance Planner — Full Implementation Specification

**Purpose of this document:** This is a build specification for an AI coding agent (Antigravity) to implement the Smart Finance Planner end-to-end. It is written to be specific and unambiguous — every screen, class, method, table, and validation rule needed is described below. Follow it as the source of truth; where the original project slides/guide are referenced, this document supersedes them with the additions requested (currency selection, investment tracking, SQL storage, and a professional UI).

---

## 1. Project Summary

Smart Finance Planner is a **desktop Java application** that helps an individual figure out the real future cost of a purchase goal (e.g., a laptop, a bike, a house down payment) by factoring in inflation, then works backward from the user's income and a chosen savings percentage to tell them how much to save monthly or yearly to reach it. Users can select a currency per goal, log actual contributions toward a goal over time, and track their progress against the plan.

This is a single-user, offline, no-login desktop tool — not a certified financial product. It is a reference/planning tool aimed at students and first-time earners.

---

## 2. Tech Stack (mandated)

| Layer | Choice | Notes |
|---|---|---|
| Language | **Java 17 (LTS)** | Core language for the whole project — no Kotlin, no Python. |
| GUI | **Swing + FlatLaf** (`com.formdev:flatlaf`) | Swing is the required GUI toolkit for this course project, but plain Swing looks dated. FlatLaf is a drop-in modern Look-and-Feel library (pure Java, no native dependencies) that gives Swing a flat, professional, IDE-quality appearance with almost no extra code — use `FlatLightLaf` as default, with an optional `FlatDarkLaf` theme toggle. |
| Database | **SQLite via JDBC** (`org.xerial:sqlite-jdbc`) | A real embedded SQL database — no server, no network, single `.db` file shipped alongside the app. All persistence must go through SQL (`CREATE TABLE`, `INSERT`, `SELECT`, `UPDATE`, `DELETE`) via `PreparedStatement`. No CSV files, no flat-file storage. |
| Build tool | **Maven** | Use a `pom.xml` with dependencies for `flatlaf`, `sqlite-jdbc`, and `junit-jupiter`. This keeps builds reproducible and matches how a grader/agent would rebuild the project. |
| Testing | **JUnit 5** | Unit tests for every calculation and DAO class (see §15). |
| Charts | **Swing custom `JPanel` painting** (no external chart library required) | Keep dependencies minimal; simple bar/line/progress visuals can be hand-drawn with `Graphics2D`. If a richer chart is wanted later, `JFreeChart` may be added, but it is not required. |
| Version control | Git | Feature branches, PRs into `main`. |

---

## 3. High-Level Architecture

Use a layered architecture. No layer should skip another (GUI never talks to SQL directly; GUI calls a Service, Service calls a DAO).

```
GUI (Swing)  →  Service layer (business logic)  →  DAO layer (SQL/JDBC)  →  SQLite database
                          ↑
                    Model classes (POJOs)
```

- **model/** — plain data classes (User, Goal, Contribution, Currency, Term).
- **engine/** (a.k.a. service layer) — all calculations and business rules: InflationCalculator, SavingsPlanner, ProgressTracker, FeasibilityChecker.
- **data/** — DAO classes that talk to SQLite: UserDao, GoalDao, ContributionDao, DatabaseManager (connection + schema init).
- **gui/** — Swing screens and reusable UI components.
- **util/** — Validator, CurrencyFormatter, DateUtil, AppTheme (FlatLaf setup + color constants).

---

## 4. Database Design (SQLite)

Use one embedded SQLite database file: `data/smart_finance_planner.db`. Create the schema programmatically on first run (`DatabaseManager.initSchema()`), using `CREATE TABLE IF NOT EXISTS`.

### 4.1 Schema (DDL)

```sql
CREATE TABLE IF NOT EXISTS users (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    name              TEXT NOT NULL,
    monthly_income    REAL NOT NULL CHECK (monthly_income > 0),
    savings_percent   REAL NOT NULL CHECK (savings_percent > 0 AND savings_percent <= 100),
    default_currency  TEXT NOT NULL DEFAULT 'INR',
    theme             TEXT NOT NULL DEFAULT 'LIGHT',
    created_at        TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS goals (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id           INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_name         TEXT NOT NULL,
    current_price     REAL NOT NULL CHECK (current_price > 0),
    target_year       INTEGER NOT NULL,
    inflation_rate    REAL NOT NULL CHECK (inflation_rate >= 0 AND inflation_rate <= 50),
    term_type         TEXT NOT NULL CHECK (term_type IN ('SHORT', 'LONG')),
    currency_code     TEXT NOT NULL DEFAULT 'INR',
    future_cost       REAL,
    monthly_saving    REAL,
    status            TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'ARCHIVED')),
    created_at        TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at        TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS contributions (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    goal_id           INTEGER NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    amount            REAL NOT NULL CHECK (amount > 0),
    contribution_date TEXT NOT NULL,
    note              TEXT,
    created_at        TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_goals_user      ON goals(user_id);
CREATE INDEX IF NOT EXISTS idx_contrib_goal     ON contributions(goal_id);
```

### 4.2 Notes

- `future_cost` and `monthly_saving` are **stored, not just computed on the fly** — they are calculated once at goal creation/edit time and persisted, so history stays stable even if formulas change later.
- `status` supports a soft "archive/complete" workflow instead of hard-deleting goals outright (see §9, Soft Delete / Archive).
- Currency is stored as an ISO-style code (`INR`, `USD`, `EUR`, `GBP`); the display symbol/formatting lives in code (`Currency.java` enum), not the database.
- All dates stored as ISO-8601 text (`YYYY-MM-DD`) for simplicity and SQLite compatibility.

---

## 5. Core Domain Logic (from the original design — implement exactly)

### 5.1 InflationCalculator
```java
double futureValue(double price, double rate, int years) // price * (1 + rate)^years
```
- `rate` is a decimal (6% → 0.06).
- Throws `IllegalArgumentException` if `price < 0` or `years < 0`.

### 5.2 SavingsPlanner
```java
double monthlySaving(double futureValue, int years)      // futureValue / max(years*12, 1)
double yearlySaving(double futureValue, int years)        // futureValue / max(years, 1)
double percentOfIncome(double monthlySaving, double monthlyIncome) // (monthlySaving/monthlyIncome)*100, 0 if income<=0
boolean isFeasible(double monthlySaving, double monthlyIncome, double savingsPercentCap)
```

### 5.3 ProgressTracker (new — supports investment tracking, §8)
```java
double getTotalContributed(int goalId)
double getProgressPercent(Goal goal)       // totalContributed / futureCost * 100, 0 if futureCost<=0
double getRemainingAmount(Goal goal)       // futureCost - totalContributed, floored at 0
boolean isOnTrack(Goal goal)               // compares elapsed-time-weighted expected savings vs actual contributed
```
`isOnTrack` logic: compute the fraction of the goal's total time elapsed since goal creation, multiply by `futureCost` to get the "expected saved by now" baseline, and compare against `getTotalContributed`. Return true if actual ≥ expected (within a small tolerance, e.g. 5%).

---

## 6. Feature List — Must-Have (from the original PPT, mapped explicitly)

All of the following from the original pitch **must** be implemented:

1. **Input Module** — collect item name, price, target year, inflation rate, monthly income, savings %, goal term (Short/Long).
2. **Inflation Engine** — compound future-value calculation.
3. **Savings Planner** — reverse-calculates required saving (monthly & yearly), income-percentage check, feasibility flag.
4. **GUI Layer** — guided multi-screen flow: Add Goal → Result → Saved Goals.
5. **Data Store** — persistent storage so goals survive an app restart (now SQLite, not CSV).
6. **Validation gate** — inputs validated before any calculation runs, with clear inline/error messaging (matches the original flow chart's "Inputs valid?" gate).
7. **Use case support** — Add Goal, View Saved Goals, Edit/Delete Goal, Set Custom Inflation Rate, View Savings Plan — all from the original use-case diagram.
8. **Default values** — sensible pre-filled defaults (inflation 6%, savings 20%) so first-time users aren't stuck guessing.

---

## 7. New Feature: Currency Selection

- Add a `Currency` enum with at least: `INR (₹)`, `USD ($)`, `EUR (€)`, `GBP (£)`.
- Each goal has its own `currencyCode` — a user can plan a UK laptop goal in GBP and an India bike goal in INR simultaneously.
- **Display-only** — no live exchange-rate conversion, no network calls. All math (inflation, savings) is unit-agnostic; only the symbol/formatting changes.
- The Add Goal screen includes a currency dropdown next to the price field, pre-filled with the user's `default_currency`.
- All monetary values shown anywhere in the app (Result screen, Saved Goals list, progress bars, contribution log) must format using that goal's currency symbol and locale-appropriate thousands separators (e.g., `₹74,300.00`, `$1,250.00`).
- A Settings screen lets the user change their own `default_currency` preference for new goals.

---

## 8. New Feature: Investment / Contribution Tracking

- From any saved goal, the user can log a **contribution**: amount, date, optional note.
- Each goal shows a **progress bar** (custom-painted or `JProgressBar`) with the label `"₹X of ₹Y saved (Z%)"`.
- A goal's detail view shows:
  - Total contributed so far
  - Remaining amount needed
  - An "on track" / "behind schedule" indicator (green check vs. amber warning), computed by `ProgressTracker.isOnTrack()`
  - A simple contribution history list (date, amount, note), most recent first
- Contributions are **manual entries only** — this is a logging/tracking tool, not a bank or investment integration. Make this explicit in the UI copy (e.g., a small helper label: "Log your own savings here — no bank connection required").
- Deleting a goal cascades and removes its contributions (`ON DELETE CASCADE` — already in schema).
- A goal automatically flips to `status = 'COMPLETED'` when `getProgressPercent(goal) >= 100`, and the Saved Goals screen visually distinguishes completed goals (e.g., a checkmark badge, muted card style).

---

## 9. Additional Features to Elevate the Project

These are recommended enhancements beyond the original scope, to make the app feel more complete and portfolio-worthy. Implement all unless the team decides to cut one for time:

1. **Dashboard / Home screen** — on launch, show a summary: total number of active goals, total saved across all goals (grouped by currency), the nearest upcoming goal deadline, and a "quick add goal" button. This becomes the app's landing screen instead of dropping straight into a bare form.
2. **Soft delete / Archive instead of hard delete** — "Delete" moves a goal to `ARCHIVED` status rather than removing the row; add an "Archived Goals" view to restore or permanently purge. Prevents accidental data loss.
3. **Edit existing goal** — a full edit flow (not just add/delete) that re-opens the Add Goal form pre-filled, recalculates future cost/monthly saving on save, and preserves existing contributions.
4. **Search & sort on Saved Goals** — a search box to filter by item name, and sort controls (by target year, by progress %, by amount).
5. **Light/Dark theme toggle** — leveraging FlatLaf's `FlatLightLaf`/`FlatDarkLaf`, stored as a per-user preference in the `users` table.
6. **Export goal summary** — a "Export" button on a goal's detail view that writes a clean plain-text or PDF summary (item, future cost, monthly target, progress, contribution history) — useful for a report appendix or sharing with family.
7. **First-run onboarding** — a short one-time setup screen (name, monthly income, default savings %, default currency) that creates the initial `users` row, instead of hardcoding a single user.
8. **Empty states** — friendly illustrations/messages when there are no goals yet ("You haven't added a goal yet — let's plan your first one") rather than a blank list.
9. **Input masks/spinners** — use `JSpinner` for year and percentage fields instead of raw text fields, reducing invalid input at the source.
10. **Keyboard shortcuts & accessibility** — Enter submits the active form, Escape cancels a dialog, tab order is logical.

---

## 10. UI / UX Design System — "clean, professional look"

Apply this consistently across every screen so the app doesn't look like a stock Swing form.

### 10.1 Color palette (use as Java `Color` constants in `AppTheme.java`)

| Token | Hex | Use |
|---|---|---|
| `NAVY_PRIMARY` | `#141B4D` | Headers, primary buttons, sidebar/nav |
| `GREEN_ACCENT` | `#2E7D4F` | Positive actions, "on track" states, progress fill |
| `AMBER_WARNING` | `#C77E1B` | "Behind schedule" indicator, validation warnings |
| `RED_ERROR` | `#C0392B` | Validation errors, delete actions |
| `SURFACE_LIGHT` | `#F7F8FA` | App background (light theme) |
| `SURFACE_DARK` | `#1B1F2A` | App background (dark theme) |
| `TEXT_PRIMARY` | `#1A1A1A` (light) / `#EDEDED` (dark) | Body text |
| `TEXT_MUTED` | `#6B7280` | Secondary/helper text |
| `CARD_BORDER` | `#E5E7EB` (light) / `#2C3140` (dark) | Card outlines/dividers |

### 10.2 Typography

- Use FlatLaf's default UI font (Segoe UI on Windows, system font elsewhere) at base size 13–14px.
- Screen titles: bold, 20–22px.
- Section labels: semi-bold, 14px, `TEXT_MUTED`.
- Currency figures (Future Cost, Monthly Saving): bold, 24–28px, `NAVY_PRIMARY` — these are the "hero numbers" and should visually dominate the Result screen.

### 10.3 Layout principles

- Generous padding (16–24px) inside cards and panels — avoid cramped Swing defaults.
- Use `CardLayout` for screen switching (as in the original design), but wrap each screen's content in a centered, max-width container (e.g., 600–700px) rather than letting fields stretch edge-to-edge on a resized window.
- Group related inputs visually with subtle card backgrounds and rounded borders (FlatLaf supports `arc` styling via `putClientProperty("JComponent.roundRect", true)` or its `FlatButton`/`FlatTextField` client properties).
- Primary actions (Calculate, Save Goal, Add Contribution) use filled `NAVY_PRIMARY` buttons with white text; secondary actions (Cancel, Back) use outlined/ghost buttons.
- Consistent iconography for goal status: ✓ complete, ⏳ active/in-progress, ⚠ behind schedule.

---

## 11. Screen-by-Screen Specification

### 11.1 Onboarding (first run only)
- Fields: Name, Monthly Income, Savings % (default 20), Default Currency (dropdown).
- "Get Started" button creates the `users` row and routes to Dashboard.

### 11.2 Dashboard (Home)
- Summary cards: Active Goals count, Total Saved (per currency, grouped if mixed), Nearest Deadline.
- List of 3–5 most recent/urgent goals as compact cards with mini progress bars.
- Prominent "+ Add New Goal" button (top-right or floating action button style).
- Nav access to: Saved Goals (full list), Settings, Archived Goals.

### 11.3 Add / Edit Goal
- Fields: Item Name, Current Price, Currency (dropdown), Target Year (spinner), Inflation Rate % (spinner, default 6), Goal Term (Short/Long radio or toggle), Monthly Income (pre-filled from user profile, editable), Savings % (pre-filled, editable).
- "Calculate" button — runs `Validator`, then `InflationCalculator` + `SavingsPlanner`, then routes to Result screen with computed values (not yet saved to DB).
- Inline validation: field-level red helper text under any invalid field (not just a popup dialog), per §13.

### 11.4 Result Screen
- Two hero numbers: **Future Cost** and **Monthly Saving Needed**, in the goal's selected currency.
- Supporting line: "Based on X% inflation, Y% of income, over N years (Short/Long-term)."
- Feasibility banner: green "This fits comfortably within your savings plan" or amber "This needs more than your target savings % — consider adjusting" (using `isFeasible()`).
- "Save This Goal" (persists to DB) and "Recalculate" (back to Add Goal form) buttons.

### 11.5 Saved Goals Dashboard
- Table or card list: item name, target year, future cost, progress bar, status badge.
- Search box + sort dropdown (§9.4).
- Per-goal actions: View Details, Edit, Archive/Delete.
- Tab or filter toggle: Active / Completed / Archived.

### 11.6 Goal Detail / Contribution Tracking
- Header: item name, target year, currency.
- Progress section: progress bar, "₹X of ₹Y saved (Z%)", on-track/behind badge.
- "+ Add Contribution" button opens a small dialog (Amount, Date, Note).
- Contribution history list below, newest first, each row showing date/amount/note with a delete icon.
- "Export Summary" button (§9.6).

### 11.7 Settings
- Default Currency, Default Savings %, Default Inflation %, Theme toggle (Light/Dark), Monthly Income update.

### 11.8 Archived Goals
- List of archived goals with "Restore" and "Delete Permanently" actions.

---

## 12. Application Flow / Navigation Map

```
Onboarding (first run only)
      ↓
   Dashboard ⇄ Settings
      ↓  ↑                    ⇄ Archived Goals
  Add/Edit Goal → Result Screen → (Save) → Saved Goals Dashboard
                                                 ↓
                                          Goal Detail (contributions)
```

---

## 13. Validation & Error Handling Rules

Implement as a reusable `Validator` class used identically by both the GUI and unit tests.

| Field | Rule | Error message |
|---|---|---|
| Item name | not blank | "Item name cannot be empty." |
| Price | numeric, > 0 | "Price must be a positive number." |
| Target year | integer, > current year | "Target year must be in the future." |
| Inflation rate | numeric, 0–50 | "Inflation rate should be between 0% and 50%." |
| Monthly income | numeric, > 0 | "Monthly income must be positive." |
| Savings % | numeric, 1–100 | "Savings % must be between 1 and 100." |
| Contribution amount | numeric, > 0 | "Contribution amount must be positive." |
| Contribution date | not in the future | "Contribution date cannot be in the future." |

General rules:
- Validate before any calculation or DB write — never let bad data reach the engine or database layer.
- All DB writes wrapped in try/catch with user-facing error dialogs (not silent console stack traces) — per the original guide's "Common Pitfalls" note about `actionPerformed` swallowing exceptions.
- Guard every division (years = 0, income = 0, futureCost = 0) to prevent crashes.
- Currency values always formatted via a single shared `CurrencyFormatter` utility — never inline `String.format` scattered across GUI classes.

---

## 14. Folder / Package Structure

```
SmartFinancePlanner/
├── pom.xml
├── src/main/java/com/sfp/
│   ├── Main.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Goal.java
│   │   ├── Contribution.java
│   │   └── Currency.java
│   ├── engine/
│   │   ├── InflationCalculator.java
│   │   ├── SavingsPlanner.java
│   │   └── ProgressTracker.java
│   ├── data/
│   │   ├── DatabaseManager.java
│   │   ├── UserDao.java
│   │   ├── GoalDao.java
│   │   └── ContributionDao.java
│   ├── gui/
│   │   ├── MainFrame.java
│   │   ├── OnboardingPanel.java
│   │   ├── DashboardPanel.java
│   │   ├── GoalFormPanel.java
│   │   ├── ResultPanel.java
│   │   ├── SavedGoalsPanel.java
│   │   ├── GoalDetailPanel.java
│   │   ├── SettingsPanel.java
│   │   ├── ArchivedGoalsPanel.java
│   │   └── components/         (reusable ProgressBarCard, GoalCard, etc.)
│   └── util/
│       ├── Validator.java
│       ├── CurrencyFormatter.java
│       ├── DateUtil.java
│       └── AppTheme.java
├── src/test/java/com/sfp/
│   ├── engine/ (InflationCalculatorTest, SavingsPlannerTest, ProgressTrackerTest)
│   └── data/   (GoalDaoTest, ContributionDaoTest — using an in-memory SQLite DB)
├── data/
│   └── smart_finance_planner.db   (created at runtime)
└── README.md
```

---

## 15. Testing Requirements

Unit tests (JUnit 5), minimum coverage:

- `futureValue()` at 0% inflation returns the original price unchanged.
- `futureValue()` matches a hand-calculated value (₹50,000 @ 6% for 5 years ≈ ₹66,911).
- `futureValue()` throws on negative price or negative years.
- `monthlySaving()` never divides by zero (years = 0 guarded).
- `percentOfIncome()` returns 0 when income is 0.
- `getProgressPercent()` returns 0 when futureCost is 0, not a crash.
- `isOnTrack()` correctly flags a goal as behind when contributions lag pace.
- `Validator` rejects every invalid case in the table in §13.
- DAO tests run against an in-memory SQLite database (`jdbc:sqlite::memory:`) to avoid touching the real data file.

Manual GUI test pass:
- Complete onboarding → land on Dashboard.
- Add a goal → see correct Result numbers → Save → appears in Saved Goals.
- Log a contribution → progress bar and on-track badge update correctly.
- Edit a goal → recalculated values persist correctly, contributions untouched.
- Archive a goal → disappears from Active, appears in Archived → Restore works.
- Close and reopen the app → all data (goals + contributions) persists via SQLite.

---

## 16. Acceptance Criteria (Definition of Done)

- [ ] All 5 original must-have modules implemented and working (§6).
- [ ] Data persists via SQLite, not flat files — schema matches §4.
- [ ] Currency selection works per-goal, display-only, at least 4 currencies supported.
- [ ] Contribution tracking works: log, view history, progress %, on-track indicator.
- [ ] UI uses FlatLaf, follows the color/typography system in §10, and every screen in §11 is implemented.
- [ ] Full navigation flow in §12 works without dead ends.
- [ ] All validation rules in §13 enforced, with no unhandled exceptions reaching the user.
- [ ] Test suite in §15 passes.
- [ ] README documents setup (`mvn install`, `mvn exec:java`) and a short usage walkthrough.

---

## 17. Build & Run

```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.sfp.Main"
```

`pom.xml` must declare dependencies for `com.formdev:flatlaf`, `org.xerial:sqlite-jdbc`, and `org.junit.jupiter:junit-jupiter` (test scope). The SQLite `.db` file should be created automatically on first launch under `data/` if it does not already exist.