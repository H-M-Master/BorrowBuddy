# BorrowBuddy

BorrowBuddy is an Android app that helps you track items and money you lend to others or borrow from them. It focuses on clear reminders, a simple workflow, and privacy-friendly local storage.

## Features

- Track both **"I lent"** and **"I borrowed"** items or money
- Store details such as item name, contact name, phone, amount, due date, notes and photo
- **Smart reminders** via notifications for upcoming and overdue items
- **One-tap actions** in notifications: mark as returned or send a polite reminder
- **Home screen widget** showing soon-due and overdue records
- Quick **contact picker** from your phone contacts
- Add a **photo** from camera or gallery
- **Swipe gestures** on the list to postpone or mark returned
- **Local backup & restore** to a JSON file
- Simple **theme settings** (system / light / dark)

## Tech Stack

- **Platform**: Android (Java, View + Fragments)
- **Architecture**: MVVM (ViewModel + Repository)
- **Persistence**: Room (SQLite)
- **Background work**: WorkManager for reminder scheduling
- **UI**: Material Components, RecyclerView, App Widgets

## Build & Run

1. Open the project in **Android Studio** (Giraffe or newer recommended).
2. Make sure you have an Android SDK (API 24+ recommended) installed.
3. Sync Gradle when prompted.
4. Run the **`app`** module on an emulator or a physical device.

## Permissions

BorrowBuddy requests:

- **Contacts**: to pick a contact name and phone number
- **Camera & Photos**: to attach a photo to a record
- **Notifications** (Android 13+): to show reminder notifications

All data is stored **locally on your device** and can be exported/imported via the backup feature.

## Screens (Overview)

- **List screen**: tabs for "I lent" / "I borrowed", swipe actions, quick overview of due status
- **Edit screen**: create or edit a record, pick contact, date, and photo
- **Settings**: theme options and local backup/restore

## License

This project is for coursework and personal use. You may browse the code and reuse ideas for learning purposes.
