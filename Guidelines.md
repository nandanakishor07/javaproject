# Smart Finance Planner — Project Guidelines

Team execution guide for Advanced Programming (Java). Companion to `SPEC.md` (the full technical spec fed to Antigravity) — this file is for the *team*: setup, workflow, timeline, and what "done" looks like. `SPEC.md` is the source of truth for exact classes/screens/schema; this file is the source of truth for *how you work together* to get there.

---

## 1. Environment Setup

| Item | Requirement |
|---|---|
| JDK | 17 or 21 (LTS) — install and confirm with `java -version` |
| IDE | Antigravity (primary), or IntelliJ IDEA / VS Code as a fallback for manual edits |
| Build tool | Maven — confirm with `mvn -version` |
| Database | No separate install needed — SQLite ships as an embedded JDBC driver dependency (`org.xerial:sqlite-jdbc`) declared in `pom.xml` |
| GUI library | FlatLaf (`com.formdev:flatlaf`) — also a Maven dependency, no separate install |
| Version control | Git + shared GitHub repo |
| Diagramming | draw.io / Visual Paradigm for keeping the class/use-case/flow diagrams in sync with the code |
| Testing | JUnit 5 (Maven dependency, test scope) |

Run `mvn clean install` once after cloning to pull all dependencies and confirm the project builds before writing any code.

---

## 2. Team Roles (map to your members)

- **2 people → Core engine & database** — `InflationCalculator`, `SavingsPlanner`, `ProgressTracker`, DAO classes (`UserDao`, `GoalDao`, `ContributionDao`), schema setup.
- **2 people → GUI** — Swing screens, FlatLaf theming, navigation flow, form validation wiring.
- **1 person → Currency & Contribution features** — `Currency` enum, `CurrencyFormatter`, `Contribution` model, contribution logging UI, progress bar / on-track logic.
- **1 person → Testing, documentation, demo** — JUnit tests, README, rehearsing the demo script, keeping diagrams in sync with final code.

---

## 3. Build Order (map this to your Gantt timeline)

Build in this order — each stage is independently testable before the next, so GUI bugs never get confused with math or database bugs.

1. **Domain models** — `User`, `Goal`, `Contribution`, `Currency`. Plain data classes, no logic beyond simple derived fields.
2. **Database layer** — `DatabaseManager` (schema creation), then `UserDao`, `GoalDao`, `ContributionDao`. Test each DAO against an in-memory SQLite DB before touching the GUI.
3. **Engine layer** — `InflationCalculator`, `SavingsPlanner`, `ProgressTracker`. Pure logic, no GUI or DB dependency — fully unit-testable in isolation.
4. **Validation** — `Validator` class, shared by GUI and tests.
5. **GUI shell** — `MainFrame` with `CardLayout`, FlatLaf theme applied, empty placeholder panels for each screen.
6. **GUI screens, one at a time** — Onboarding → Dashboard → Add/Edit Goal → Result → Saved Goals → Goal Detail (contributions) → Settings → Archived Goals. Wire each into the engine/DAO layers as you build it.
7. **Integration + testing + polish** — full click-through, edge cases, currency formatting pass, UX copy cleanup, demo rehearsal.

---

## 4. Git Workflow

1. One member creates the repo, pushes the initial folder structure (from `SPEC.md` §14) and `pom.xml` to `main`.
2. Everyone branches per feature: `feature/inflation-engine`, `feature/goal-dao`, `feature/currency-selector`, `feature/contribution-tracking`, `feature/dashboard-ui`, etc.
3. Small, frequent commits with clear messages (e.g., `"Add futureValue calculation + unit test"`).
4. Pull requests reviewed by at least one teammate before merging — even a short "looks good" comment is enough, but it demonstrates collaboration in your report.
5. Tag a `v1.0` release once integration is complete, before the demo.
6. If using Antigravity to generate large chunks of code, review the diff before merging — don't merge agent output unreviewed.

---

## 5. Testing Checklist

**Unit tests (JUnit 5), at minimum:**
- `futureValue()` at 0% inflation returns the original price unchanged.
- `futureValue()` matches a hand-calculated value (₹50,000 @ 6% for 5 years ≈ ₹66,911).
- `futureValue()` throws on negative price or negative years.
- `monthlySaving()` never divides by zero (years = 0 guarded).
- `percentOfIncome()` returns 0 when income is 0.
- `getProgressPercent()` returns 0 when futureCost is 0, no crash.
- `isOnTrack()` correctly flags a goal as behind schedule.
- `Validator` rejects every invalid input case (see `SPEC.md` §13).
- DAO tests run against an in-memory SQLite DB, not the real data file.

**Manual GUI pass:**
- Complete onboarding → land on Dashboard.
- Add a goal → correct Result numbers → Save → appears in Saved Goals.
- Log a contribution → progress bar and on-track badge update correctly.
- Edit a goal → recalculates correctly, contributions untouched.
- Archive a goal → disappears from Active, appears in Archived → Restore works.
- Switch currency on a new goal → all figures format with the right symbol.
- Toggle light/dark theme → persists after restart.
- Close and reopen the app → all data persists via SQLite.

---

## 6. Suggested Timeline

| Phase | Duration | Owner(s) |
|---|---|---|
| Requirement analysis | Done (slides) | Whole team |
| UML & system design | Done (slides) | Whole team |
| Database schema + DAO layer | 2 days | Core/DB pair |
| Core engine (Inflation, Savings, Progress) | 2–3 days | Core/DB pair |
| GUI shell + navigation | 1–2 days | GUI pair |
| GUI screens (all, wired to engine/DAOs) | 4–5 days | GUI pair (parallel with currency/contribution work) |
| Currency + contribution tracking feature | 3–4 days | Feature owner |
| Integration + testing + polish | 2–3 days | Testing owner + everyone fixes their own bugs |
| Documentation & demo rehearsal | 1–2 days | Whole team |

Tip: agree on DAO and engine method signatures on Day 1 so the GUI pair can build against stubs (dummy return values) without waiting on the engine/DB pair to finish.

---

## 7. Common Pitfalls to Avoid

- **Divide-by-zero** on years = 0, income = 0, or futureCost = 0 — guard every division.
- **Floating point display** — always format through `CurrencyFormatter`, never `String.format("₹%.2f", ...)` scattered across GUI classes (this also breaks once currency becomes selectable).
- **Blocking the EDT** — keep DB calls fast; if a query ever feels slow, wrap it in a `SwingWorker` rather than freezing the UI.
- **Swallowed exceptions** — never do DB or file I/O directly inside a button's `actionPerformed` without a try/catch and a user-facing error dialog, or failures will silently vanish into the console.
- **GUI talking directly to SQL** — always go through a DAO; never write raw JDBC calls inside a Swing panel.
- **Mismatched diagrams vs. code** — if a class changes during coding (fields, methods), update the class diagram in your final report; graders check both.
- **Hardcoded single currency** — don't let `₹` leak back into any GUI class as a literal; always resolve it from the goal's `Currency`.
- **No default values** — pre-fill sensible defaults (inflation 6%, savings 20%) so first-time users aren't stuck guessing.
- **Merging agent-generated code unreviewed** — always read the diff, run the tests, and sanity-check against `SPEC.md` before merging.

---

## 8. Submission Checklist

- [ ] All model, engine, DAO, and GUI classes from `SPEC.md` implemented.
- [ ] SQLite schema matches `SPEC.md` §4, data persists across restarts.
- [ ] Currency selection working across all screens (display-only, ≥4 currencies).
- [ ] Contribution tracking working: log, history, progress %, on-track indicator.
- [ ] All 8 screens from `SPEC.md` §11 implemented and reachable via the nav map in §12.
- [ ] FlatLaf theming applied, light/dark toggle working.
- [ ] Input validation matches `SPEC.md` §13, no unhandled exceptions reach the user.
- [ ] Unit + DAO test suite passes (`mvn test`).
- [ ] Class diagram and code are consistent.
- [ ] README with setup/run instructions (`mvn clean install`, `mvn exec:java`).
- [ ] Final report includes updated diagrams, test results, and screenshots of every screen.
- [ ] Demo script rehearsed: Onboarding → Add Goal → Result → Save → Log Contribution → Saved Goals → Edit/Archive → reopen app → data persists.

---

Good luck — this scope is realistic for a 6-person team as long as the database/engine layer (steps 1–4 in §3) locks down early so the GUI pair isn't blocked, and the currency/contribution feature is built against the same DAO contracts from the start rather than bolted on at the end.