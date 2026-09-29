# Inkwell

A near-zero-friction diary app for Android. Each day is a running log of
manually-started, timestamped blocks — tap "+", write, done.

## What's built

- **Today** — the core loop. Stream of today's blocks, tap "+" to open an
  inline composer with optional quick tag/mood chips, tap the check to save
  with the current timestamp. Tap any block's text to edit it in place (the
  timestamp itself never changes).
- **History** — every day that has entries, newest first, with a block
  count. Tap a day to open it (read + edit existing text; new blocks only
  ever attach to *today*, by design — see `data/Block.kt`).
- **Search** — debounced search across block text, tags, and moods, with
  results showing their date.
- **Settings** — accent color, font pairing (Classic serif / Modern sans /
  Typewriter), paper-grain intensity, quick tag/mood management, biometric
  lock toggle, and Google Drive sync.
- **Lock** — off by default. When enabled, gates the whole app behind
  `androidx.biometric` (fingerprint/face/device PIN, whatever the device
  offers).
- **Drive sync** — backs up all blocks as one JSON file in the app's hidden
  Drive `appDataFolder` (invisible in the user's normal Drive UI). Sync is a
  simple merge: every block has a client-generated UUID and an `editedAt`
  timestamp, so combining two devices' data is just "keep whichever copy of
  each id was edited more recently" — no text-diff conflict UI needed.

## Architecture notes

- `data/Block.kt` — one row per timestamped block. `id` is a UUID, not an
  auto-incrementing Long — that matters the moment two phones sync, since
  two devices autoincrementing from 1 would otherwise collide.
- `data/UserPreferences.kt` — DataStore-backed settings, reactive via Flow.
- `data/drive/` — `DriveSyncManager` talks to the Drive v3 REST API
  directly over OkHttp rather than pulling in the full
  `google-api-services-drive` client library, to keep the app lightweight.
- `ui/AppRoot.kt` — loads settings, applies the theme, and gates on the
  lock screen before showing `ui/navigation/InkwellNavHost.kt`.

## Setting up Google Drive sync

Sync needs a Google Cloud project you control — this can't be pre-configured
for you since it's tied to your own OAuth credentials:

1. Go to the [Google Cloud Console](https://console.cloud.google.com/),
   create a project (or reuse one).
2. **APIs & Services → Library** — enable the **Google Drive API**.
3. **APIs & Services → OAuth consent screen** — configure it (External is
   fine for testing with your own Google account; add yourself as a test
   user while it's unpublished).
4. Get your debug keystore's SHA-1:
   `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
5. **APIs & Services → Credentials → Create Credentials → OAuth client ID**
   → Application type **Android** → package name `com.john.inkwell` → paste
   the SHA-1 from step 4.
6. No client secret or config file is needed in the app itself —
   `GoogleSignInOptions` + the Play Services sign-in flow resolve the client
   ID from your app's package name + signing certificate automatically, as
   long as steps 1–5 are done for that exact package name and SHA-1.
7. For a **release build**, repeat step 4–5 with your release keystore's
   SHA-1 and add a second Android OAuth client for it.

Once that's done, install the app, go to Settings → Google Drive sync →
Sign in with Google, grant the (very narrow) `drive.appdata` scope, and tap
Sync now.

## Known simplifications (fine for personal use, worth hardening later)

- `BiometricGate.availability()` exists but isn't yet checked before
  turning the lock on in Settings — if a device has no biometrics/PIN
  enrolled and the user enables the lock, they'd need to enroll one to get
  back in. Worth wiring up an availability check + warning dialog.
- Drive sync is manual (a "Sync now" button), not automatic/background.
  `WorkManager` would be the natural next step for periodic background sync.
- No conflict UI — by design (see above) — but that also means there's no
  way to see *what* changed during a merge, only the resulting count.

## Opening the project

1. Open this folder in Android Studio (Koala or newer).
2. Let it generate the Gradle wrapper on first sync (or run
   `gradle wrapper --gradle-version 8.7` from this folder if you have
   Gradle installed locally) — the wrapper jar itself isn't checked in.
3. Run on a device/emulator with API 26+.
4. Drive sync won't work until you've done the Cloud Console setup above —
   everything else (writing, history, search, customization, lock) works
   immediately with no setup.

## Package

`com.john.inkwell` — application ID and namespace are both set in
`app/build.gradle.kts`.
