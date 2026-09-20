# Smart Finance Planner

> A desktop Java financial planning application built with Java 17, Swing, FlatLaf, SQLite JDBC, and JUnit 5.

Smart Finance Planner helps individuals calculate the real future cost of purchase goals (laptops, vehicles, education, real estate down payments) by compounding annual inflation, then works backward from their monthly income and target savings rate to recommend feasible monthly and yearly savings plans. The application supports multi-currency display, manual contribution tracking with pacing indicators, soft-delete archiving, and a modern desktop UI.

---

## 🌟 Key Features

1. **Compound Inflation Engine**: Calculates future costs using compound inflation: $\text{price} \times (1 + \text{rate})^\text{years}$.
2. **Reverse Savings Planner**: Calculates required monthly and yearly savings targets, income consumption percentages, and checks affordability against user-defined savings caps.
3. **Pacing & Progress Tracking**: Real-time progress bars (`₹X of ₹Y saved (Z%)`) and an intelligent on-track vs. behind-schedule indicator based on elapsed time.
4. **Multi-Currency Display**: Full display support for INR (`₹`), USD (`$`), EUR (`€`), and GBP (`£`) per goal and per user profile without exchange rate conversion dependencies.
5. **Robust SQLite Persistence**: Embedded SQLite storage (`data/smart_finance_planner.db`) using strict `PreparedStatement` JDBC, active foreign key cascades (`PRAGMA foreign_keys = ON;`), and automatic schema generation.
6. **Soft Delete & Archiving**: Move goals to an archive to keep your active dashboard clean, with instant restore or permanent purge capabilities.
7. **Report Export**: One-click plain-text summary report export (`.txt`) via file dialog for easy sharing or archiving.
8. **Modern FlatLaf Design**: Clean, modern look-and-feel with curated color tokens (`NAVY_PRIMARY`, `GREEN_ACCENT`, `AMBER_WARNING`), responsive `CardLayout` navigation, non-blocking asynchronous `SwingWorker` threads, and dynamic Light/Dark theme toggling.

---

## 🏗️ Architecture

```
gui/ (Swing + FlatLaf)  ──►  engine/ (pure logic)  ──►  data/ (DAO SQL)  ──►  SQLite (.db)
                                  ▲
                          model/ (POJOs)
```

- **`com.sfp.model`**: `User`, `Goal`, `Contribution`, `Currency`.
- **`com.sfp.engine`**: `InflationCalculator`, `SavingsPlanner`, `ProgressTracker` (zero Swing and zero JDBC dependencies).
- **`com.sfp.data`**: `DatabaseManager`, `UserDao`, `GoalDao`, `ContributionDao`.
- **`com.sfp.gui`**: `MainFrame`, `OnboardingPanel`, `DashboardPanel`, `GoalFormPanel`, `ResultPanel`, `SavedGoalsPanel`, `GoalDetailPanel`, `SettingsPanel`, `ArchivedGoalsPanel`.
- **`com.sfp.gui.components`**: `CustomProgressBar`, `GoalCard`, `StatCard`, `Navbar`.
- **`com.sfp.util`**: `Validator`, `CurrencyFormatter`, `DateUtil`, `AppTheme`.

---

## 💻 Tech Stack & Requirements

| Layer | Technology |
|---|---|
| **Language** | Java 17 (or Java 21+ with `release 17` compatibility) |
| **GUI Framework** | Swing + FlatLaf 3.6 |
| **Database** | SQLite via SQLite JDBC 3.49.1.0 (`org.xerial:sqlite-jdbc`) |
| **Build & Tooling** | Apache Maven 3.9.9 via Maven Wrapper (`mvnw` / `mvnw.cmd` / `mvn.cmd`) |
| **Testing** | JUnit 5 (`org.junit.jupiter:junit-jupiter:5.11.3`) |

---

## 🚀 Quick Start & Running the App

No global Maven installation is required. The repository includes the official Maven Wrapper (`mvnw.cmd` on Windows, `./mvnw` on Linux/macOS) and a root `mvn.cmd` helper.

### 1. Run Unit Tests
```bash
# Windows
.\mvnw.cmd test
# Or using the wrapper helper
.\mvn.cmd test
# Linux / macOS
./mvnw test
```

### 2. Launch the Application
```bash
# Windows
.\mvnw.cmd exec:java
# Or
.\mvn.cmd exec:java
# Linux / macOS
./mvnw exec:java
```

### 3. Build a Distribution JAR
```bash
.\mvnw.cmd package -DskipTests
```
The compiled JAR is located at `target/smart-finance-planner-1.0.0.jar`.

---

## 🧭 Application Walkthrough & Demo Script

1. **First-Time Onboarding**:
   - On first launch, enter your name (e.g., *Alex*), monthly income (e.g., *₹60,000*), target savings rate (e.g., *25%*), and default currency (*INR*).
   - Click **Get Started** to persist your profile to SQLite and open the Dashboard.

2. **Planning a Purchase Goal**:
   - Click **+ Add New Goal**.
   - Enter Item Name: *MacBook Pro*, Current Price: *150,000*, Currency: *INR*, Target Year: *2027*, Inflation Rate: *6%*.
   - Click **Calculate Savings Plan →**.
   - Review the **Result Screen**: Hero numbers show the compound future cost (~₹178,652.40) and required monthly saving (~₹4,962.57), accompanied by a feasibility confirmation banner.
   - Click **Save This Goal ✓**.

3. **Tracking Progress & Contributions**:
   - From the Dashboard or Saved Goals, click **View Details** on the goal card.
   - Click **+ Log Contribution**, enter amount *20,000*, and submit.
   - The progress bar and on-track status indicator instantly update, and the contribution entry appears in the history log.

4. **Exporting Summary**:
   - In Goal Details, click **📄 Export Summary (.txt)** to choose a destination and save a clean plain-text summary report.

5. **Archiving & Settings**:
   - Click **Archive** on any goal to remove it from the active dashboard.
   - Open **Archived Goals** to review or click **Restore to Active**.
   - Open **Settings** to adjust income, default savings rate, or toggle between Light and Dark themes.

---

## 🧪 Testing Coverage

The automated test suite runs against in-memory SQLite instances (`jdbc:sqlite::memory:`) and tests all calculation engines and validation rules:
- `InflationCalculatorTest`: 0% rate tests, 0 year tests, negative bounds guards, hand-calculated compound verification.
- `SavingsPlannerTest`: Zero-year division guards, zero-income percentage guards, savings cap feasibility tests.
- `ProgressTrackerTest`: Zero future cost guards, remaining amount flooring, deterministic date-injected pacing checks (`isOnTrack`).
- `ValidatorTest`: Verification for all 8 validation constraints in `specs.md` §13.
- `UserDaoTest`, `GoalDaoTest`, `ContributionDaoTest`: Insert, query, update, soft-delete, and `ON DELETE CASCADE` foreign key verification.
