# AI Budget Planner Mobile Application

Version 1.0 scaffold for an offline-first AI-powered budget planner.

## Implemented in this scaffold

- Android project using Kotlin + Jetpack Compose + MVVM + Hilt + Room + DataStore setup
- Local budget engine with:
  - available budget calculation
  - daily budget calculation
  - remaining budget and savings progress
- Expense tracking flow:
  - setup profile
  - manage fixed expenses
  - add expense
  - expense history with search/filter/edit/delete
  - bank statement import
  - SMS-based expense auto-capture
  - dashboard summary
  - AI insights screen (warnings, suggestions, risk, health score)
- FastAPI backend with endpoints for:
  - auth (`/register`, `/login`, `/logout`)
  - profile (`/profile`)
  - expenses CRUD (`/expenses`)
  - reports (`/reports/monthly`, `/reports/yearly`)
  - AI (`/predict`, `/recommendations`, `/financial-score`)

## Project structure

- `android-app/`: Android client
- `backend/`: FastAPI service

## Android setup

1. Open `android-app` in Android Studio.
2. Let Gradle sync complete.
3. Create a `google-services.json` in `android-app/app/` if Firebase sync is enabled.
4. Run on emulator/device.

## Backend setup

1. Create virtual environment and install dependencies:

```powershell
cd backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

2. Run API:

```powershell
uvicorn app.main:app --reload
```

3. Open Swagger UI:

- `http://127.0.0.1:8000/docs`

## Module coverage map

- Module 1 User Profile: implemented (local + backend)
- Module 2 Fixed Expenses: implemented with Android CRUD UI (add/list/edit/delete), category picker, recurring toggle, and backend CRUD APIs
- Module 3 Expense Tracker: implemented with Android add + expense history (search/filter/edit/delete) and backend CRUD
- Module 4 Budget Engine: implemented with dedicated calculation functions for available, daily, remaining budgets and savings progress
- Module 5 AI Recommendation Engine: implemented with warning and suggestion generation in Android and backend recommendation APIs
- Module 6 Prediction Engine: implemented with Android on-device TensorFlow Lite inference pipeline (with heuristic fallback if model file is unavailable) and backend model-based prediction using RandomForest and XGBoost fallback
- Module 7 Financial Health Score: implemented with 0-100 score and category mapping (Excellent/Good/Average/Needs Improvement) in backend and Android insights UI
- Module 8 Reports: implemented backend monthly/yearly aggregation endpoints and Android reports screen with daily/weekly/monthly/yearly summaries plus category and monthly trend charts
- Module 9 Bank Statement Import: implemented with Android file picker/upload flow and backend parsing for CSV, Excel, and PDF statements with auto-categorization and duplicate detection
- Module 10 SMS Expense Reader: implemented with SMS receiver, runtime permission flow, richer NLP-style entity extraction (merchant/account/reference), expanded multi-bank templates, auto-categorization, persistent duplicate prevention, and automatic local expense creation
- Security Hardening: implemented encrypted Room database (SQLCipher + secure passphrase storage) and app startup lock with biometric authentication and PIN fallback
- Cloud Sync: implemented Firebase Firestore sync with timestamp-based conflict resolution (last-write-wins by updatedAt), bidirectional merge, and manual sync status controls
- Test Coverage: implemented baseline unit tests (budget engine, SMS parser), Compose UI test (setup flow), and FastAPI API tests (auth, expenses CRUD, financial score)
- Offline Replay Sync: implemented pending-operation queue (profile/expense/fixed-expense upsert/delete), immediate one-time sync trigger after local writes, and periodic background sync scheduling with WorkManager
- Deep Test Coverage: added sync conflict policy tests, statement import parser/duplicate fixture tests, and backend end-to-end workflow API tests

## Next implementation priorities

1. Add sync observability and retry analytics (queue depth, failure reasons, and replay success metrics) in profile/debug UI.
2. Add integration tests for background sync worker retry behavior and pending queue replay failure recovery.
