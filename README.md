# Smart Library Management App
### COMP 30040 – Mobile Application Development | Spring 2026 | Middle East College

---

## Project Overview
An Android mobile application for managing library books, members, and overdue fines.
Built with Java + XML + Firebase Realtime Database.

---

## Screens
| Screen | Description |
|--------|-------------|
| Splash | 3-second loading screen → auto-navigates to Login |
| Login | Firebase Authentication with email/password |
| Register | New user registration stored in Firebase |
| Book Management | Full CRUD – Insert, Update, Delete, View All |
| Transaction | Overdue fine calculator (Days × OMR 0.100) |
| Report | Search and view all book records |

---

## Setup Instructions

### 1. Firebase Setup (REQUIRED)
1. Go to [https://console.firebase.google.com](https://console.firebase.google.com)
2. Create a new project named **smart-library-mec**
3. Add an Android app with package: `com.smartlibrary.app`
4. Download `google-services.json` and place it in `/app/` folder
5. Enable **Authentication → Email/Password**
6. Enable **Realtime Database** and set rules to:
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

### 2. Android Studio Setup
1. Open Android Studio
2. File → Open → select the `SmartLibraryApp` folder
3. Wait for Gradle sync to complete
4. Connect an Android device or start an emulator (API 24+)
5. Click Run ▶

---

## Firebase Database Structure
```
smart-library-mec
├── books
│   └── {bookId}
│       ├── bookId: "-NxKp8a2mB3"
│       ├── title: "Introduction to Algorithms"
│       ├── author: "Thomas H. Cormen"
│       ├── isbn: "9780262046305"
│       └── category: "Technology"
└── members
    └── {uid}
        ├── uid: "abc123"
        ├── name: "Ahmed Al-Rashdi"
        ├── email: "ahmed@mec.edu.om"
        ├── phone: "+96891234567"
        └── role: "Librarian"
```

---

## Fine Calculation Formula
```
Fine (OMR) = Days Overdue × 0.100
Example: 5 days × 0.100 = OMR 0.500
```

---

## Tech Stack
- **Language:** Java
- **UI:** XML Layouts with Material Components
- **Backend:** Firebase Realtime Database
- **Auth:** Firebase Authentication
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)

---

## Version Control
This project uses Git for version control. See commit history for development progress.

---

*Middle East College — Department of Computing — Spring 2026*
