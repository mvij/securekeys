# Play Store release checklist (v4.0)

Everything SecureKeys v4.0 needs before it goes on Google Play, in three areas: security, policy, and the store listing. Each item is checked against the repo as of commit `877ad14` and against Google's help pages as of 27 November 2026. Sources are listed at the end.

**Status key**

- ✅ **Done**: already in the repo or already true.
- ⬜ **To do**: work that can be done in the repo or by preparing assets.
- 👤 **Needs the developer**: needs a Play Console login, a decision, a key, or an identity document.

**Developer details:** Vichitran · contact@mvij.xyz · https://mvij.xyz

---

## 0. Blockers to settle first

Two items decide whether v4.0 ships as an **update** to the old app or as a **new app**. Settle them before anything else.

| # | Item | Status |
|---|------|--------|
| 0.1 | **Is `com.dexetra.securekeys` already on Google Play, and in which developer account?** The 2014 app (v3.0, `versionCode 1`) used this application ID. If it's live (or was ever published) in a Dexetra account or any account other than Vichitran's, you need a [Play app transfer](https://support.google.com/googleplay/android-developer/answer/6230247) into the Vichitran account. If you can't get one, v4.0 needs a new application ID, and existing users won't get it as an upgrade. A package name can never be reused by another account, even after the app is unpublished. | 👤 |
| 0.2 | **Do you still have the keystore that signed the 2014 release?** Updating an existing listing needs the original app signing key. You either upload it to Play App Signing, or the app is already enrolled and you only need its upload key. If the key is lost and the app isn't enrolled in Play App Signing, the listing can't be updated. You'd have to publish under a new application ID. | 👤 |
| 0.3 | **Turkish-locale passphrase mismatch (see 1.1).** If 0.1 means this is an upgrade for existing users, fix this before release. | ⬜ |

---

## 1. Security

### 1.1 Password algorithm compatibility

| # | Item | Status |
|---|------|--------|
| 1.1.1 | Kotlin port matches the original AAR byte for byte on an English-locale device: 192 + 9,000 golden vectors in `src/test/resources/`, pinned by `PasswordGeneratorTest`, run in CI. | ✅ |
| 1.1.2 | **Fix the Turkish/Azerbaijani lowercase mismatch.** The old AAR called `String.toLowerCase()` with the device's default locale (`tools/golden/generate.sh` says so, and pins the vectors to `en-US`). `PasswordGenerator.generate` now uses `lowercase(Locale.ROOT)`. On a device set to Turkish (`tr`) or Azerbaijani (`az`), the old app turned `I` into dotless `ı` (U+0131) and `İ` into `i`. v4.0 turns `I` into `i` and `İ` into `i̇` (two characters). Any passphrase with a capital `I` or `İ` gives a **different password after the upgrade** for those users. Suggested fix: lowercase with `Locale.getDefault()`, which matches the legacy behaviour exactly, and update the README's "How it works" note. | ⬜ |
| 1.1.3 | Add Turkish-locale golden vectors: run the AAR with `-Duser.language=tr -Duser.country=TR` over phrases containing `I`, `İ`, `ı` and `i`, then add a test that sets the default locale to `tr-TR` and checks them. | ⬜ |
| 1.1.4 | Check the other legacy defaults: `getBytes()` on Android is always UTF-8, so the port's explicit UTF-8 already matches. No further work unless the golden generator shows otherwise. | ✅ |
| 1.1.5 | Before release, install the old 3.0 APK, write down a few passwords, then upgrade in place to the v4.0 release build and confirm they're the same (one passphrase with a capital `I`, one with spaces, one with non-ASCII). | 👤 |

### 1.2 Signing and Play App Signing

| # | Item | Status |
|---|------|--------|
| 1.2.1 | Release signing comes from a git-ignored `secure_keystore.properties`; `*.jks` and `*.keystore` are git-ignored. | ✅ |
| 1.2.2 | Enrol in **Play App Signing**. It's required for new apps, which must ship as AABs. Google holds the app signing key and you sign uploads with a separate **upload key**. For an existing app (0.2), you upload the original key during enrolment. | 👤 |
| 1.2.3 | Create the upload key (if new) and point `secure_keystore.properties` at it. Back up the keystore and its passwords in two places outside the repo, such as a password manager and an offline copy. | 👤 |
| 1.2.4 | Check that no keystore or `secure_keystore.properties` was ever committed: `git log --all -- '*.jks' '*.keystore' secure_keystore.properties`. | ⬜ |
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
| 1.4.3 | Only the launcher activity is exported. | ✅ |

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
| 1.7.1 | Dependency surface is tiny: Compose BOM, Material 3, activity-compose, lifecycle-viewmodel-compose; JUnit only for tests. No analytics, ads, crash or network SDKs. | ✅ |
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
| 2.2.3 | Optionally link the policy from the in-app Help screen. Not required for an app with no data collection, but a nice touch. | ⬜ |

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
| 2.4.3 | `minSdk = 26` (Android 8.0). Users of the old app on Android 7.1 or older will stay on 3.0. Fine, but worth knowing. | ✅ |

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
| 2.6.3 | **Impersonation / IP**: the old Dexetra name is in the package ID only. Make sure the listing, icon and screenshots don't use another company's brand. | ✅ |
| 2.6.4 | **User Data policy**: met by no collection plus a privacy policy (2.2, 2.3). | ⬜ |

---

## 3. Play Store resources

### 3.1 Build and versioning

| # | Item | Status |
|---|------|--------|
| 3.1.1 | New apps must be uploaded as an **Android App Bundle (AAB)**. Build with `./gradlew :securekeys:bundleRelease` (output: `securekeys/build/outputs/bundle/release/securekeys-release.aab`). Add this command to README and CLAUDE.md, which only mention `assembleRelease` today. | ⬜ |
| 3.1.2 | `versionCode = 2`, `versionName = "4.0"`. If the old app is on Play (0.1), the version code must be higher than the highest one ever uploaded there; check in the old listing's App bundle explorer. | 👤 |
| 3.1.3 | Tag the release commit `v4.0` and write release notes ("What's new", 500 characters max per language). | ⬜ |
| 3.1.4 | Release notes draft: *"SecureKeys 4.0 is a full rewrite with a new look. Your passwords stay the same: the same passphrase and options still give the same password. Now with light and dark themes, a clearer help screen, and the clipboard cleared when you close the app."* | ⬜ |

### 3.2 Testing tracks

| # | Item | Status |
|---|------|--------|
| 3.2.1 | **Internal testing** first (up to 100 testers, available within minutes, no Data safety form needed). Check install, upgrade from 3.0 (1.1.5) and the minified build. | 👤 |
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
> • Tap the key to copy. The password is marked sensitive so it's hidden from clipboard previews, and the clipboard is cleared when you close the app.
> • Light and dark themes that follow your system, with a manual toggle.
> • Built-in help.
>
> Upgrading from an earlier version? Version 4.0 is a complete rewrite with a new design, and your passwords are unchanged.
>
> Tip: use a passphrase only you know, and vary it per site (for example by adding the site's name) to get a different password for each one.

### 3.4 Graphics

| # | Item | Status |
|---|------|--------|
| 3.4.1 | **App icon**: 512 × 512 px, 32-bit PNG with alpha, up to 1 MB. Export from the adaptive vector (`drawable/ic_launcher_foreground.xml` on the teal background) at full bleed; Play applies the mask itself. | ⬜ |
| 3.4.2 | **Feature graphic**: 1024 × 500 px, JPEG or 24-bit PNG (no alpha). Required. Suggested: the brass key on blued steel with the wordmark in Archivo Expanded, matching the brand in `ui/Theme.kt`. `design/brand-preview.png` in the project files is a reference, not the right size. | ⬜ |
| 3.4.3 | **Phone screenshots**: 2 to 8, PNG or JPEG, up to 8 MB, 320 to 3,840 px per side, longest side at most 2.3× the shortest. To be eligible for recommendations, provide **at least 4 at 1080 × 1920 portrait or larger**. Suggested set: empty generator, options and length set, brass key showing a password, dark theme, help screen. Use a sample passphrase, never a real one. | ⬜ |
| 3.4.4 | **Tablet screenshots** (7" and 10"): at least 4, 1,080 to 7,680 px, 16:9 or 9:16. Optional, but improves visibility on large screens. Check the layout on a tablet first. | ⬜ |
| 3.4.5 | **Alt text** for the feature graphic and screenshots (140 characters each). | ⬜ |
| 3.4.6 | Preview video: optional (YouTube, public or unlisted, ads off). Skip for v4.0. | ✅ |

### 3.5 Final pre-submit pass

| # | Item | Status |
|---|------|--------|
| 3.5.1 | All Play Console *Dashboard > Set up your app* tasks show complete. | 👤 |
| 3.5.2 | Countries and regions selected; price set to free (it can't go from free to paid later). | 👤 |
| 3.5.3 | Signed AAB uploaded to internal testing, installed from Play, upgrade from 3.0 checked (1.1.5). | 👤 |
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
> **What the app stores on your device.** Only your theme choice (light or dark) if you change it, so the app remembers it next time. It stays on your device.
>
> **Clipboard.** When you tap to copy a password, it's placed on your device's clipboard, where other apps you allow may read it, as with any copied text. SecureKeys marks it as sensitive and clears the clipboard when you close the app.
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
