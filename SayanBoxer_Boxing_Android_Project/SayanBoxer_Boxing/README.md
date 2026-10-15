# Sayan Boxer – Boxing Training (Android)

An original offline boxing training app inspired by the **functionality**, not the code or branding, of guided boxing workout apps like PunchFit.

**Android:** Android 8.0+ (API 26), including Android 16 (API 36 devices). `compileSdk 35`, `targetSdk 35`.

## Features

- Configurable round count (1–20), work duration (15–600 s), rest duration (5–180 s), callout frequency (3–45 s)
- Beginner, Intermediate, Advanced and HIIT presets
- Speech coach uses built-in Android TextToSpeech; punch combinations called randomly
- Add/remove combinations with numbers 1–6 plus slip/duck/block/step back
- Foreground timer and voice coaching with a persistent notification
- Pause, resume and stop from notification, workout screen or background
- Audible round bell, session history and simple local statistics
- Offline, no login or accounts, no tracking or ads, developer credit Sayan Boxer

### Build (Android Studio)
Open this folder as a Gradle project. Install API 35 and Java 17. Build -> Build APK(s) to create a debug-signed installable APK. For a release, use Build -> Generate Signed Bundle / APK.

### Build without Android Studio (GitHub Actions)
Create a private or public GitHub repository and upload **the contents** of this folder (including `.github/workflows/build-apk.yml`). Push to `main`. In the repository, open the Actions tab and run **Build Sayan Boxer APK**, or wait for the push-triggered build. Once it finishes, download the `SayanBoxer-boxing-debug-apk` artifact. Unzip it and install the included `app-debug.apk` on the Android phone. This is a debug-signed testing build, not a release-signed Play Store APK.

### Voice troubleshooting
Android's Speech Services / Text-to-Speech voice must be installed and enabled. A first spoken prompt may be silent while the TTS engine initializes. Ensure media volume is audible. On phones with aggressive battery optimization, allow Sayan Boxer to run in the background. Depending on Android settings, notification permission may be required for visible workout controls.

### Notes and limits
- This is not a copy of PunchFit, and does not include its copyrighted assets.
- Workouts are not automatically counted by sensors. Completed rounds, time and sessions are tracked by the app.
- A warm-up and proper coaching are recommended. This is not medical advice.
- The APK has **not been compiled or tested in this execution environment** because the Android SDK, Gradle and network access are unavailable. Build and device behavior require testing, especially Android 16 background execution behavior.
