# Zenith '26 College Fest Management System 🚀

A complete, full-stack college fest management system comprising:
1. **Native Android Application** built with **Kotlin**, **Jetpack Compose (Material 3)**, **Kotlin Coroutines**, and **Retrofit/OkHttp**.
2. **Backend API** built with **Python 3.11+**, **FastAPI**, **Pydantic v2**, and **Uvicorn**.
3. **Database** powered by **SQLite** (`fest.db`), with automated initial seeding via `openpyxl` from `data/events_seed.xlsx`.
4. **Reference Web Frontend** in `frontend/` (`index.html`, `admin.html`, `js/app.js`, `js/admin.js`).
5. **API Contract Verification Suite** in `zenith26-api.postman_collection.json`.

---

## 🛠️ Architecture & Tech Stack

```text
  ┌─────────────────────────────────────────────────────────┐
  │                 Android Frontend (Kotlin)               │
  │     Jetpack Compose • Material 3 • Coroutines • Retrofit │
  │        [Student Portal]      [Admin Dashboard]          │
  └───────────────────────────┬─────────────────────────────┘
                              │ HTTP / JSON
                              ▼
  ┌─────────────────────────────────────────────────────────┐
  │                 FastAPI Backend (Python)                │
  │       REST API Endpoints • Business Logic & Validation   │
  └───────────────────────────┬─────────────────────────────┘
                              │ SQL Queries
                              ▼
  ┌─────────────────────────────────────────────────────────┐
  │               SQLite Database (fest.db)                 │
  │        events table (14 seeds) • registrations table     │
  └─────────────────────────────────────────────────────────┘
```

- **Android Frontend:** Kotlin (JVM 1.8/21), Jetpack Compose, Material 3, ViewModel, Coroutines, Retrofit 2, OkHttp 3, Gson.
- **Backend:** Python 3.11+, FastAPI, Pydantic v2, Uvicorn.
- **Database:** SQLite (`fest.db`), seeded with 14 fest events from `data/events_seed.xlsx`.
- **API Testing:** Postman Desktop (`zenith26-api.postman_collection.json`) and automated JVM test suite (`ZenithAppTest.kt`).

---

## 📂 Project Structure

```text
Mobile app activity 1/
├── android/                             # Native Android Kotlin Application
│   ├── app/
│   │   ├── build.gradle.kts             # App build config, Compose, Retrofit
│   │   ├── proguard-rules.pro           # ProGuard rules
│   │   └── src/
│   │       ├── main/
│   │       │   ├── AndroidManifest.xml   # Manifest with INTERNET & cleartext traffic
│   │       │   ├── java/org/zenith26/fest/
│   │       │   │   ├── ZenithApplication.kt      # Dynamic Base URL management
│   │       │   │   ├── MainActivity.kt           # Screen navigation & top app bar
│   │       │   │   ├── data/
│   │       │   │   │   ├── model/               # Event, Registration & Stats models
│   │       │   │   │   ├── api/                 # Retrofit service & NetworkModule
│   │       │   │   │   └── repository/          # ZenithRepository
│   │       │   │   └── ui/
│   │       │   │       ├── theme/               # Slate/Purple/Emerald Tailwind palette
│   │       │   │       ├── common/              # Navbar, Category badges, state views
│   │       │   │       ├── student/             # Student portal & registration dialog
│   │       │   │       ├── admin/               # 4 Stat cards, utilization table, cancel
│   │       │   │       └── settings/            # ServerConfigDialog (emulator/device IP)
│   │       │   └── res/                         # Strings, colors, drawables, mipmaps
│   │       └── test/java/org/zenith26/fest/
│   │           └── ZenithAppTest.kt             # Automated JVM unit & live API tests
│   ├── build.gradle.kts                 # Root Android Gradle build script
│   ├── settings.gradle.kts              # Project settings
│   ├── gradle.properties                # JVM & AndroidX settings
│   ├── local.properties                 # Android SDK path configuration
│   └── gradlew.bat                      # Android Gradle wrapper launcher
├── backend/
│   ├── database.py                      # SQLite connection, pragmas, table schemas & init_db
│   ├── models.py                        # Pydantic schemas for events, registration & live stats
│   ├── seed_data.py                     # Automated Excel reader & database seeder
│   └── main.py                          # FastAPI app, API routes, CORS, static file serving
├── frontend/                            # Reference web frontend (preserved as source of truth)
│   ├── index.html
│   ├── admin.html
│   └── js/ (app.js, admin.js)
├── data/
│   └── events_seed.xlsx                 # 14 sample fest events spreadsheet
├── fest.db                              # SQLite database file
├── gradlew.bat                          # Convenience wrapper at workspace root
├── requirements.txt                     # Backend Python dependencies
├── zenith26-api.postman_collection.json # Postman test collection
└── README.md
```

---

## 🚀 Running the FastAPI Backend

### 1. Set Up Python Environment
From the project root:
```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

### 2. Verify Seed Data
```powershell
python -m backend.seed_data
```

### 3. Start the FastAPI Server
```powershell
uvicorn backend.main:app --host 0.0.0.0 --port 8000
```
> **Note:** Using `--host 0.0.0.0` ensures the server accepts connections from localhost, Android emulators (`10.0.2.2`), and physical Android devices on the local network.

Interactive API docs are available at:
- Swagger UI: [http://localhost:8000/docs](http://localhost:8000/docs)
- ReDoc: [http://localhost:8000/redoc](http://localhost:8000/redoc)

---

## 📱 Building and Running the Native Android App

### 1. Build the Debug APK via Gradle
From the project root, run:
```powershell
.\gradlew.bat assembleDebug
```
Or from the `android/` directory:
```powershell
cd android
.\gradlew.bat assembleDebug
```
The compiled APK will be generated at:
`android/app/build/outputs/apk/debug/app-debug.apk`

### 2. Run Automated Tests
```powershell
.\gradlew.bat testDebugUnitTest
```
Runs the 5 automated test suites:
- Model JSON serialization/deserialization
- Client-side validation logic
- Live API integration against running FastAPI backend
- Stats deserialization
- Utilization calculations & 90% threshold highlight

### 3. Install on Android Device or Emulator
```powershell
adb install android\app\build\outputs\apk\debug\app-debug.apk
```

---

## 🌐 Connecting Android App to the Backend

The Android app features a built-in **Server Configuration Dialog** (accessible via the ⚙️ gear icon in the top app bar) to switch environments without recompiling:

| Target Environment | Backend Base URL | Configuration Instructions |
|---|---|---|
| **Android Emulator** | `http://10.0.2.2:8000/` | Default out-of-the-box preset. Android emulator automatically maps `10.0.2.2` to host PC localhost. |
| **Physical Device (Wi-Fi)** | `http://<YOUR_PC_IP>:8000/` | Tap ⚙️ in the app, enter your computer's local IP (e.g., `http://192.168.1.15:8000/`), and tap **Save & Reconnect**. |
| **Physical Device (USB ADB Reverse)** | `http://localhost:8000/` | Run `adb reverse tcp:8000 tcp:8000`. Then select the **Localhost** preset in the app. |

---

## 🎨 UI & Feature Mapping (Web to Native Android)

| Feature | Original Web App (`index.html` / `admin.html`) | Native Android App (`Jetpack Compose`) |
|---|---|---|
| **Branding & Top Bar** | Gradient "ZENITH '26" + "Annual Fest" badge | `FestTopAppBar` with gradient brush + navigation tabs + ⚙️ settings |
| **Hero Section** | Campus dates pill, tagline, description | Gradient hero banner matching Tailwind palette |
| **Search Input** | Debounced search input with clear button | `OutlinedTextField` with 250ms coroutine debounce & clear button |
| **Category Filters** | Horizontal pills (All, Technical, Cultural...) | Scrollable `CategoryBadge` pills with active purple styling |
| **Event Cards** | Emoji icon, category badge, fee/FULL badge, seats left | `EventCardItem` in Slate 800 with hover/elevation & border |
| **Event Details** | Modal card with date, time, venue, description | `EventDetailDialog` full-screen responsive dialog |
| **Student Registration** | Form with Name, Roll, Email, Phone | Validated form with inline errors & server error alert banners |
| **Seat Availability** | Live count update on cards after registration | `ViewModel` automatic refresh on 201 response |
| **Live Stats Cards** | 4 cards: Events, Registrations, Utilization %, Upcoming | `AdminStatCard` grid with category distribution pills |
| **Event Utilization Table** | Table sorted by utilization descending, >=90% warning | `AdminDashboardScreen` list sorted descending, Rose 950 warning highlight |
| **Registration Management** | Registered students table with event filter | Filterable student list with cancellation confirmation dialog |
| **Add New Event (Stretch)** | Modal form adding event | `AddEventDialog` creating events via `POST /api/events` |

---

## 📡 API Contract Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/events` | List all events with live `available_seats`. Query params: `category`, `search`. |
| `GET` | `/api/events/{event_id}` | Retrieve details for a specific event (or `404 Not Found`). |
| `POST` | `/api/events/{event_id}/register` | Register a student with strict server-side validation (`201`, `400`, `404`, `409`). |
| `GET` | `/api/registrations` | List all registrations with event titles. Query param: `event_id`. |
| `GET` | `/api/stats` | Live fest metrics (total events, registrations, capacity %, category breakdown). |
| `POST` | `/api/events` | Coordinator creates a new fest event (`201`). |
| `DELETE` | `/api/registrations/{reg_id}` | Coordinator cancels a registration, restoring seats (`200`). |

---

## 🧪 Postman & Automated Testing

1. Open Postman Desktop App.
2. Click **Import** and select `zenith26-api.postman_collection.json`.
3. Set `base_url` = `http://localhost:8000`.
4. Run all requests to verify:
   - **Events:** List, category filter (`category=Technical`), search (`search=robo`), event details, 404 test.
   - **Registrations:** 201 Created, 400 validation error (empty fields / invalid email), 409 conflict ("Already registered" / "Event is full").
   - **Admin:** Live statistics (`/api/stats`), registrations list with event filtering.
   - **Stretch goals:** Add new event, cancel registration.
