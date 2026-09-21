# Family Safety — transparent Android app

This is a real Android Studio project for **consensual family location sharing**. It deliberately does not include hidden camera/microphone recording or covert tracking.

## What is included
- Firebase email/password sign-in using Firebase REST APIs.
- Shared family code for grouping devices.
- Visible Start/Stop Location Sharing controls.
- Android foreground location service with persistent notification while sharing is ON.
- Periodic GPS/network location updates to Firestore.
- Family member location list and Google Maps hand-off.
- No camera or microphone access.

## Free backend setup
1. Create a Firebase project at https://console.firebase.google.com/.
2. Enable Authentication > Email/Password.
3. Create a Firestore database.
4. In Firebase project settings, copy the **Web API key** and **Project ID**.
5. Firestore rules should be configured so only authenticated family members can read/write the required family paths. Do not use open `allow read, write: if true` rules.
6. Install the APK on each participating phone, enter the same Firebase API key/project ID, create/sign in to an account, and use the same family code.

## Build
Open the project folder in Android Studio and use Build > Build APK(s).

## Important limitation
The app needs a Firebase project because remote multi-phone location sharing requires a backend. Firebase has a no-cost tier, but quotas/rules can change. This project does not provide a shared backend or secretly host one for you.

## Privacy
Every participating device controls its own sharing state. Android's visible foreground-service notification is shown while sharing is active. This app does not implement hidden camera, microphone, keylogging, stealth mode, or silent tracking.
