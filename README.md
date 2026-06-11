<<<<<<< HEAD
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
- Module 6 Prediction Engine: implemented with on-device trend prediction in Android and backend model-based prediction using RandomForest and XGBoost fallback
- Module 7 Financial Health Score: implemented with 0-100 score and category mapping (Excellent/Good/Average/Needs Improvement) in backend and Android insights UI
- Module 8 Reports: implemented backend monthly/yearly aggregation endpoints and Android reports screen with daily/weekly/monthly/yearly summaries plus category and monthly trend charts
- Module 9 Bank Statement Import: implemented with Android file picker/upload flow and backend parsing for CSV, Excel, and PDF statements with auto-categorization and duplicate detection
- Module 10 SMS Expense Reader: implemented with SMS receiver, runtime permission flow, multi-template debit parsing, auto-categorization, persistent duplicate prevention, and automatic local expense creation

## Next implementation priorities

1. Add TensorFlow Lite model inference pipeline for on-device prediction.
2. Improve SMS parsing with richer NLP entity extraction and expanded multi-bank templates.
3. Add encrypted Room + biometric/PIN lock.
4. Add Firebase sync conflict resolution.
=======
# App_Balance
>>>>>>> 660e0cf04b5a62b768294e415f6ca842c72a5e5d
