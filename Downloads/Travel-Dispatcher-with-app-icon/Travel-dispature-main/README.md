# Travel Dispatcher — Native Android & Web Portal

A private, single-owner native Android application (Kotlin, Jetpack Compose, Material 3, minSdk 26) and companion web portal for travel company owners.

## Architecture Highlights
- **Serverless & Self-Contained:** No custom backend servers, no Edge Functions, no cloud runtimes to maintain.
- **Supabase (PostgreSQL + Auth):** Managed through `supabase-kt` (`postgrest-kt`, `auth-kt`) with Ktor OkHttp client engine and `kotlinx.serialization`.
- **Direct JavaMail SMTP:** Sends quotation request emails directly to partner travel agents from the device over `smtp.gmail.com:465` SSL using `com.sun.mail:android-mail` and `android-activation`.
- **Privacy By Design:** The email builder function strictly omits the client phone number. All export features default to masking client contact details.
- **Hardware-Backed Encryption:** Gmail App Password is encrypted using the Android Keystore (`androidx.security:security-crypto` AES256-GCM) and is never printed in logs or displayed after saving.
- **Strict Row-Level Security & Triggers:** PostgreSQL database triggers with row locks (`FOR UPDATE`) reject disbursements exceeding received balances and prevent lowering received amounts below disbursed funds.

---

## 1. Supabase Setup Guide

### Step 1: Create Supabase Project
1. Log in to [supabase.com](https://supabase.com) and create a new project.
2. Note your **Project URL** (`https://<project-ref>.supabase.co`) and **Anon / Public Key** from **Project Settings > API**.

### Step 2: Run Database Migration
1. In the Supabase Dashboard, open **SQL Editor**.
2. Open `supabase/migrations/001_init.sql` from this repository.
3. Paste the contents into the SQL Editor and click **Run**.
4. This sets up all tables (`app_owner`, `agents`, `inquiries`, `inquiry_stops`, `dispatches`, `ledger_records`, `ledger_sends`), row lock triggers, over-send protection, and strict RLS policies.

### Step 3: Run SQL Tests (Optional but Recommended)
1. In SQL Editor, open and run `supabase/tests/001_init_test.sql`.
2. Verify that all trigger and cascading deletion tests report `SUCCESS` in the output log.

### Step 4: Create the Single Owner User
1. Go to **Authentication > Users** in Supabase.
2. Click **Add User > Create User**.
3. Enter your owner email (e.g. `owner@traveldispatcher.local` or your personal email) and choose a secure password.
4. Copy the generated User **UID** (UUID format).
5. In the **SQL Editor**, run:
   ```sql
   INSERT INTO public.app_owner (id, email)
   VALUES ('<PASTE-COPIED-USER-UID-HERE>', 'owner@traveldispatcher.local');
   ```
   *Only users explicitly present in `public.app_owner` are granted read/write access by RLS policies.*

### Step 5: Disable Public Signups
1. Go to **Authentication > Configuration > Providers > Email**.
2. Turn **OFF** the toggle: **"Allow new users to sign up"**.
3. Save changes. This ensures no external users can register.

---

## 2. Gmail SMTP & App Password Setup

To dispatch quotation request emails directly to partner agents:
1. Open your Google Account at [myaccount.google.com](https://myaccount.google.com).
2. Go to **Security > 2-Step Verification** (must be enabled).
3. Scroll to **App passwords** (or search "App passwords").
4. Create a new App Password with the name **"Travel Dispatcher"**.
5. Copy the 16-character generated password (e.g., `abcd efgh ijkl mnop`).
6. In Travel Dispatcher, navigate to **Settings > EMAIL ACCOUNT (GMAIL SMTP)**:
   - Enter your Gmail Address.
   - Enter the 16-character App Password.
   - Set your preferred sender display name (e.g. `Travel Dispatcher`).
   - Tap **"Save Email Settings"**.
   - Tap **"Send test email to myself"** to verify connection over port 465 SSL.

---

## 3. Configuring Android Studio & Building the APK

### Step 1: Configure Credentials
In the `android/` directory, copy `local.properties.example` to `local.properties` (or create a `.env` file in the project root):
```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
*The Gradle build script automatically resolves credentials from `local.properties`, `.env`, Gradle properties, or environment variables in priority order.*

### Step 2: Open in Android Studio
1. Open Android Studio (Ladybug / Hedgehog or newer with JDK 17+).
2. Select **Open** and choose the `android` folder.
3. Allow Gradle to sync dependencies.

### Step 3: Run Unit Tests
To verify privacy and ledger protection rules:
```bash
cd android
./gradlew test
```
- `EmailBuilderTest`: Validates client phone exclusion and HTML sanitization.
- `LedgerRulesTest`: Validates over-spending limits and balance calculation.

### Step 4: Build Debug APK
```bash
cd android
./gradlew assembleDebug
```
The debug APK is generated at:
`android/app/build/outputs/apk/debug/app-debug.apk`

### Step 5: Build a Signed Release APK
1. In Android Studio, go to **Build > Generate Signed Bundle / APK...**
2. Choose **APK** and click **Next**.
3. Create or choose a release Keystore (`release.keystore`).
4. Select `release` build variant with V1 and V2 (Full APK Signature) enabled.
5. Click **Finish**.
Or via command line:
```bash
./gradlew assembleRelease
```
The APK can be installed directly on any Android device running Android 8.0 (API 26) or higher.

---

## 4. Web Application Preview

This workspace also hosts the live interactive web portal recreating the exact same UI, UX, cards, bottom sheets, and workflows shown in screenshots 01 to 09.
- Use the bottom navigation to switch between **Inquiries**, **New**, **Ledger**, **Agents**, and **Settings**.
- Try status transitions (`I (Pending)`, `C (Confirmed)`, `x (Cancelled)`), dispatch bottom sheet, live ledger calculations, and PDF/CSV exports.
- Download the complete Android Studio project zip archive anytime via the built-in exporter.

---

## Author & Credits
- **Client & Owner:** Jaykumar Mistry
- **Product:** Travel Dispatcher


## App launcher icon

The Travel Dispatcher launcher icon is installed in `android/app/src/main/res/`. Density-specific PNG icons support older launchers, and adaptive icon XML resources are provided for Android 8.0+ (minSdk 26). The app manifest already references `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`.
