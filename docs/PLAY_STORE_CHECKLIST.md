# Play Store release checklist (v4.0)

Everything SecureKeys v4.0 needs before it goes on Google Play, in three areas: security, policy, and the store listing. Each item is checked against the repo as of commit `b6ccadd` (after the splash/Settings screen and the package rename) and against Google's help pages as of 27 November 2026. Sources are listed at the end.

**Status key**

- ✅ **Done**: already in the repo or already true.
- ⬜ **To do**: work that can be done in the repo or by preparing assets.
- 👤 **Needs the developer**: needs a Play Console login, a decision, a key, or an identity document.

**Developer details:** Vichitran · contact@mvij.xyz · https://mvij.xyz

---

## 0. Package and account (decided)

| # | Item | Status |
|---|------|--------|
| 0.1 | **Application ID is `xyz.mvij.securekeys`**, published from the Vichitran developer account with a new upload keystore. The 2014 app (`com.dexetra.securekeys`, v3.0) is left as it is. v4.0 is a **new Play listing**, so old users won't get it as an automatic update: they install it fresh, and their passwords stay the same because the algorithm is unchanged. | ✅ |
| 0.2 | Mention the old app in the listing ("Used SecureKeys before? Your passphrases still give the same passwords") so former users can find and trust the new listing. | ⬜ |
| 0.3 | **Turkish-locale passphrase mismatch (see 1.1.2).** Former users on Turkish or Azerbaijani phones would get different passwords from the same passphrase. Fix before release. | ⬜ |

---

## 1. Security

### 1.1 Password algorithm compatibility

| # | Item | Status |
|---|------|--------|
| 1.1.1 | Kotlin port matches the original AAR byte for byte on an English-locale device: 192 + 9,000 golden vectors in `src/test/resources/`, pinned by `PasswordGeneratorTest`, run in CI. | ✅ |
| 1.1.2 | **Fix the Turkish/Azerbaijani lowercase mismatch.** The old AAR called `String.toLowerCase()` with the device's default locale (`tools/golden/generate.sh` says so, and pins the vectors to `en-US`). `PasswordGenerator.generate` now uses `lowercase(Locale.ROOT)`. On a device set to Turkish (`tr`) or Azerbaijani (`az`), the old app turned `I` into dotless `ı` (U+0131) and `İ` into `i`. v4.0 turns `I` into `i` and `İ` into `i̇` (two characters). Any passphrase with a capital `I` or `İ` gives a **different password in v4.0 than in the old app** for those users. Suggested fix: lowercase with `Locale.getDefault()`, which matches the legacy behaviour exactly, and update the README's "How it works" note. | ⬜ |
| 1.1.3 | Add Turkish-locale golden vectors: run the AAR with `-Duser.language=tr -Duser.country=TR` over phrases containing `I`, `İ`, `ı` and `i`, then add a test that sets the default locale to `tr-TR` and checks them. | ⬜ |
| 1.1.4 | Check the other legacy defaults: `getBytes()` on Android is always UTF-8, so the port's explicit UTF-8 already matches. No further work unless the golden generator shows otherwise. | ✅ |
| 1.1.5 | Before release, install the old 3.0 APK and the v4.0 release build side by side (different application IDs, so both fit), and check that a few passphrases give the same password in both: one with a capital `I`, one with spaces, one with non-ASCII. | 👤 |

### 1.2 Signing and Play App Signing

| # | Item | Status |
|---|------|--------|
| 1.2.1 | Release signing comes from a git-ignored `secure_keystore.properties`; `*.jks` and `*.keystore` are git-ignored. | ✅ |
| 1.2.2 | Enrol in **Play App Signing** when creating the app (required for new apps, which ship as AABs). Let Google generate the app signing key; you sign uploads with the Vichitran **upload key**. | 👤 |
| 1.2.3 | Create the Vichitran upload keystore and point `secure_keystore.properties` at it. Back up the keystore and its passwords in two places outside the repo, such as a password manager and an offline copy. A lost upload key can be reset through Play support, but it takes days. | 👤 |
| 1.2.4 | No keystore or `secure_keystore.properties` was ever committed (`git log --all -- '*.jks' '*.keystore' secure_keystore.properties` is empty). | ✅ |
| 1.2.5 | Optional: a CI job that builds a signed AAB from GitHub secrets. Until then, release builds are made locally. | ⬜ |

### 1.3 Build hardening

| # | Item | Status |
|---|------|--------|
| 1.3.1 | R8 minify and resource shrinking on for `release` (`isMinifyEnabled = true`, `isShrinkResources = true`, `proguard-android-optimize.txt`). | ✅ |
| 1.3.2 | Release build isn't debuggable (AGP default; no `isDebuggable` override). | ✅ |
| 1.3.3 | Smoke-test the minified release build on a device. The app has no reflection or serialization, so the empty `proguard-rules.pro` should be enough, but only a real run proves it. | 👤 |
| 1.3.4 | Upload the R8 `mapping.txt` with each release (Play Console > App bundle explorer > Downloads), or let Play take it from the AAB, so Android vitals stack traces are readable. | 👤 |
| 1.3.5 | 16 KB page-size support: no native libraries, so nothing to do. | ✅ |

### 1.4 Permissions and manifest

| # | Item | Status |
|---|------|--------|
| 1.4.1 | The manifest declares **no permissions**, including no `INTERNET`. The app can't send anything off the device. | ✅ |
| 1.4.2 | Check the **merged** release manifest (`securekeys/build/intermediates/merged_manifests/release/`) for permissions added by libraries. AndroidX adds a signature-level `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, which is harmless; anything else should be removed with `tools:node="remove"`. | ⬜ |
| 1.4.3 | The app's own manifest exports only the launcher activity. Confirm in the merged manifest (1.4.2) that library components (for example androidx.profileinstaller's receiver, guarded by `android.permission.DUMP`) are permission-protected. | ✅ |

### 1.5 Backups and stored data

| # | Item | Status |
|---|------|--------|
| 1.5.1 | `android:allowBackup="false"`. Passphrase and password live only in `GeneratorViewModel` and are never written to disk. | ✅ |
| 1.5.2 | The only thing stored is the theme override (`current_theme`) in SharedPreferences. On Android 12+, `allowBackup=false` doesn't stop device-to-device transfer. Add `android:dataExtractionRules` excluding everything if you want a clean statement. Low risk either way, since it's only a theme choice. | ⬜ |

### 1.6 Clipboard and screen

| # | Item | Status |
|---|------|--------|
| 1.6.1 | Copied password is flagged `EXTRA_IS_SENSITIVE` on Android 13+, so it's hidden from clipboard previews. | ✅ |
| 1.6.2 | Clipboard is cleared when the activity finishes, but only if this app copied. | ✅ |
| 1.6.3 | Swiping the app away from Recents, or the process being killed, skips `onDestroy`, so the password stays on the clipboard. Consider also clearing it after a timeout (for example 60 seconds) and saying so in the "copied" line. | ⬜ |
| 1.6.4 | The Recents thumbnail and screenshots can show the generated password. Consider `setRecentsScreenshotEnabled(false)` (API 33+) or `FLAG_SECURE` while a password is on screen. `FLAG_SECURE` also blocks the screenshots you need for the listing, so make it a release-only toggle or take screenshots from a debug build. | ⬜ |
| 1.6.5 | Check on a device that the passphrase field opens a password keyboard with no suggestions or learning. `GeneratorScreen` passes its own `KeyboardOptions(imeAction = Go)` to `OutlinedSecureTextField`; make sure that doesn't drop the secure field's default password keyboard type. | 👤 |

### 1.7 Dependencies and code checks

| # | Item | Status |
|---|------|--------|
| 1.7.1 | Dependency surface is tiny: Compose BOM (ui, Material 3, material-icons-core, ui-tooling-preview), activity-compose, lifecycle-viewmodel-compose, core-splashscreen; ui-tooling in debug only; JUnit only for tests. No analytics, ads, crash or network SDKs. | ✅ |
| 1.7.2 | Unit tests and Android lint run in CI on every push and PR. | ✅ |
| 1.7.3 | Add Dependabot (or Renovate) for the `gradle` and `github-actions` ecosystems so library and action updates arrive as PRs. | ⬜ |
| 1.7.4 | Make the CI check required on `main` (branch protection) so a failing test or lint error can't be merged before a release. | 👤 |
| 1.7.5 | MD5 is used as a deterministic mixing function, not to store secrets, and can't change without breaking users' passwords. Keep listing and help text honest about what the app does (see 2.6). | ✅ |

---

## 2. Policy

### 2.1 Developer account

| # | Item | Status |
|---|------|--------|
| 2.1.1 | Play Console developer account for **Vichitran**. Decide **personal vs organization**: an organization account needs a D-U-N-S number and shows the organization name; a personal account shows your legal name or chosen developer name. | 👤 |
| 2.1.2 | Complete **identity verification** in Play Console, and verify the contact email (contact@mvij.xyz) and phone number. | 👤 |
| 2.1.3 | **Android developer verification**: rolled out to all Play Console developers from March 2026. If your Play identity is verified, Google auto-registers your Play apps. Enforcement on certified devices started 30 September 2026 in Brazil, Indonesia, Singapore and Thailand, with a global rollout in 2027. Check the per-app registration status on Play Console's *Android developer verification* page after creating the app. | 👤 |
| 2.1.4 | **New personal accounts** (created after 13 November 2023) must run a **closed test with at least 12 testers opted in for 14 days in a row** before they can apply for production access. Google's review of that application usually takes up to 7 days. Plan for about three weeks between first upload and public launch. This doesn't apply to organization accounts or older personal accounts. | 👤 |

### 2.2 Privacy policy

| # | Item | Status |
|---|------|--------|
| 2.2.1 | **A privacy policy is required for every app**, including apps that collect no data. The Data safety form can't be submitted without the link. | ⬜ |
| 2.2.2 | Host it on a public, non-PDF, non-geofenced page, for example `https://mvij.xyz/securekeys/privacy`. It must name the developer (Vichitran) and give a contact (contact@mvij.xyz). A draft is in the appendix. | 👤 |
| 2.2.3 | Optionally add a privacy-policy row to Settings > About: a `developer_privacy_url` string in `res/values/developer.xml` plus a row in `SettingsScreen`. Not required for an app with no data collection, but a nice touch. | ⬜ |

### 2.3 Data safety form

| # | Item | Status |
|---|------|--------|
| 2.3.1 | Fill in the form even though nothing is collected. Google defines "collect" as sending data off the device; data processed only on the device doesn't need to be declared. With no `INTERNET` permission and no SDKs, the answers are: **Does your app collect or share any of the required user data types? No.** | 👤 |
| 2.3.2 | Optional badges: "Data is encrypted in transit" doesn't apply (nothing is sent). There's no account, so there's no deletion mechanism to declare. | 👤 |
| 2.3.3 | Recheck the form whenever a dependency or permission is added (for example crash reporting). | ⬜ |

### 2.4 Target API level

| # | Item | Status |
|---|------|--------|
| 2.4.1 | New apps and updates must target **API 36 (Android 16)** since 31 August 2026. `targetSdk = 36`. | ✅ |
| 2.4.2 | Plan for the next bump. Google usually raises the bar each August; third-party sources expect API 37 in August 2027 (not yet on Google's page). `compileSdk` is already 37.2. | ⬜ |
| 2.4.3 | `minSdk = 26` (Android 8.0). Phones on Android 7.1 or older can't install the new listing; they can keep using the old 3.0 app. | ✅ |

### 2.5 App content declarations (Play Console > Policy > App content)

| # | Item | Status |
|---|------|--------|
| 2.5.1 | **Content rating** (IARC questionnaire): utility app, no user-generated content, no communication, no purchases. Expect "Everyone" / PEGI 3. | 👤 |
| 2.5.2 | **Target audience and content**: pick 13+ or 18+ and say the app isn't designed for children, so the Families policy doesn't apply. | 👤 |
| 2.5.3 | **App access**: all functionality is available without login or special access. | 👤 |
| 2.5.4 | **Ads**: no ads. | 👤 |
| 2.5.5 | Other declarations: no news, government, financial, health or COVID features; no advertising ID use (declare "No" on the advertising ID form). | 👤 |

### 2.6 Policies that touch password apps

There's no Play policy specific to password generators. These are the ones that apply:

| # | Item | Status |
|---|------|--------|
| 2.6.1 | **Deceptive behaviour / misleading claims**: don't overstate security in the listing ("military-grade", "unhackable", "encrypted vault"). Describe it as it is: a deterministic generator that stores nothing. | ⬜ |
| 2.6.2 | **Accessibility API and Autofill/Credential Manager** policies cover password managers that fill other apps. SecureKeys uses neither, so they don't apply. Revisit if autofill is ever added. | ✅ |
| 2.6.3 | **Impersonation / IP**: the application ID is now `xyz.mvij.securekeys`, matching the developer's own domain. Make sure the listing, icon and screenshots don't use another company's brand (including Dexetra's). | ✅ |
| 2.6.4 | **User Data policy**: met by no collection plus a privacy policy (2.2, 2.3). | ⬜ |

---

## 3. Play Store resources

### 3.1 Build and versioning

| # | Item | Status |
|---|------|--------|
| 3.1.1 | New apps must be uploaded as an **Android App Bundle (AAB)**. Build with `./gradlew :securekeys:bundleRelease` (output: `securekeys/build/outputs/bundle/release/securekeys-release.aab`); the command is in README and CLAUDE.md. | ✅ |
| 3.1.2 | `versionCode = 2`, `versionName = "4.0"`. It's a new listing, so any version code works; keep 2 and only ever go up. | ✅ |
| 3.1.3 | Tag the release commit `v4.0` and write release notes ("What's new", 500 characters max per language). | ⬜ |
| 3.1.4 | Release notes draft: *"First release of the new SecureKeys. If you used the old app, your passphrases still give the same passwords. New design, light and dark themes, built-in help, and the copied password is hidden from clipboard previews."* | ⬜ |

### 3.2 Testing tracks

| # | Item | Status |
|---|------|--------|
| 3.2.1 | **Internal testing** first (up to 100 testers, available within minutes, no Data safety form needed). Check install, the side-by-side comparison with 3.0 (1.1.5) and the minified build. | 👤 |
| 3.2.2 | Read the **pre-launch report** (automatic on test tracks) for crashes, accessibility and security warnings. | 👤 |
| 3.2.3 | **Closed testing** with 12+ testers for 14 days if 2.1.4 applies, then apply for production. | 👤 |
| 3.2.4 | **Production** with a staged rollout (for example 20%, then 50%, then 100%), watching Android vitals between steps. | 👤 |

### 3.3 Listing text (main store listing)

| # | Item | Status |
|---|------|--------|
| 3.3.1 | **App name** (30 characters max): `SecureKeys`, or `SecureKeys: Password Generator` (30) for search. | ⬜ |
| 3.3.2 | **Short description** (80 max). Draft (73): *"Turn one passphrase into strong, repeatable passwords. Nothing is stored."* | ⬜ |
| 3.3.3 | **Full description** (4,000 max). Draft below. | ⬜ |
| 3.3.4 | **Category**: Tools. **Tags**: password generator, security, privacy. | 👤 |
| 3.3.5 | **Contact details**: email contact@mvij.xyz (required, shown publicly), website https://mvij.xyz, phone optional. | 👤 |
| 3.3.6 | **Privacy policy URL** (see 2.2). | 👤 |

Full description draft:

> SecureKeys turns a simple passphrase into a strong password, and gives you the same password every time.
>
> Type a passphrase, choose a length from 5 to 25 characters, and pick what to mix in: uppercase letters, numbers, symbols. The same passphrase with the same options always produces the same password, on any device running SecureKeys. So there's nothing to store, sync or back up.
>
> • Nothing is stored. No database, no account, no cloud. Your passphrase and password exist only in memory while the app is open.
> • No internet access. The app has no network permission, so nothing can leave your phone.
> • Tap the key to copy. The password is marked sensitive so it's hidden from clipboard previews, and the clipboard is cleared when you exit the app.
> • Light and dark themes that follow your system, or pick Light or Dark in Settings.
> • No ads, no tracking, works fully offline.
> • Built-in help.
>
> Used SecureKeys before? This is a complete rewrite with a new design, and the same passphrase and options still give the same password as the original app.
>
> Tip: use a passphrase only you know, and vary it per site (for example by adding the site's name) to get a different password for each one.

### 3.4 Graphics

| # | Item | Status |
|---|------|--------|
| 3.4.1 | **App icon**: 512 × 512 px, 32-bit PNG with alpha, up to 1 MB. Export from the adaptive vector (`drawable/ic_launcher_foreground.xml` on the teal background) at full bleed; Play applies the mask itself. | ⬜ |
| 3.4.2 | **Feature graphic**: 1024 × 500 px, JPEG or 24-bit PNG (no alpha). Required. Suggested: the brass key on blued steel with the wordmark in Archivo Expanded, matching the brand in `ui/Theme.kt`. The brand preview image shared earlier (`design/brand-preview.png`, outside the repo) is a style reference, not the right size. | ⬜ |
| 3.4.3 | **Phone screenshots**: 2 to 8, PNG or JPEG, up to 8 MB, 320 to 3,840 px per side, longest side at most 2.3× the shortest. To be eligible for recommendations, provide **at least 4 at 1080 × 1920 portrait or larger**. Suggested set: empty generator, options and length set, brass key showing a password, dark theme, help screen. Use a sample passphrase, never a real one. | ⬜ |
| 3.4.4 | **Tablet screenshots** (7" and 10"): at least 4, 1,080 to 7,680 px, 16:9 or 9:16. Optional, but improves visibility on large screens. Check the layout on a tablet first. | ⬜ |
| 3.4.5 | **Alt text** for the feature graphic and screenshots (140 characters each). | ⬜ |
| 3.4.6 | Preview video: optional (YouTube, public or unlisted, ads off). Skip for v4.0. | ✅ |

### 3.5 Final pre-submit pass

| # | Item | Status |
|---|------|--------|
| 3.5.1 | All Play Console *Dashboard > Set up your app* tasks show complete. | 👤 |
| 3.5.2 | Countries and regions selected; price set to free (it can't go from free to paid later). | 👤 |
| 3.5.3 | Signed AAB uploaded to internal testing, installed from Play, passwords match 3.0 (1.1.5). | 👤 |
| 3.5.4 | CI green on the release commit; release tagged. | ⬜ |

---

## Appendix: privacy policy draft

> **SecureKeys privacy policy**
>
> Last updated: [date]
>
> SecureKeys is published by Vichitran ("we"). This policy explains how the SecureKeys Android app handles information.
>
> **We don't collect any data.** SecureKeys generates passwords entirely on your device. Your passphrase and generated passwords are kept only in your device's memory while the app is open. They are never saved to storage, backed up, or sent anywhere. The app has no internet permission and contains no analytics, advertising or tracking code.
>
> **What the app stores on your device.** Only your theme choice, if you pick Light or Dark in Settings, so the app remembers it next time (choosing "Match system" removes it). It stays on your device.
>
> **Clipboard.** When you tap to copy a password, it's placed on your device's clipboard, where other apps you allow may read it, as with any copied text. SecureKeys marks it as sensitive so it's hidden from clipboard previews, and clears the clipboard when you exit the app with Back. If the app is swiped away or stopped by the system first, the password stays on the clipboard until something else replaces it.
>
> **Children.** SecureKeys isn't directed at children and collects no information from anyone.
>
> **Changes.** If this policy changes, we'll update this page and the date above.
>
> **Contact.** contact@mvij.xyz · https://mvij.xyz

---

## Sources

Checked 27 November 2026.

- [Target API level requirements for Google Play apps](https://support.google.com/googleplay/android-developer/answer/11926878)
- [App testing requirements for new personal developer accounts](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Provide information for Google Play's Data safety section](https://support.google.com/googleplay/android-developer/answer/10787469)
- [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151)
- [Android developer verification: rolling out to all developers](https://android-developers.googleblog.com/2026/03/android-developer-verification-rolling-out-to-all-developers.html) (Android Developers Blog, March 2026)
- [Transfer apps to a different developer account](https://support.google.com/googleplay/android-developer/answer/6230247)
- [Use Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756)
