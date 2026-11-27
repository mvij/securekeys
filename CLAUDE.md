# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

SecureKeys: Android password generator. Kotlin + Jetpack Compose (Material 3), single module `:securekeys`, package `com.dexetra.securekeys`. Toolchain: Gradle 9.9.2 (wrapper), AGP 9.5.3 (built-in Kotlin), Kotlin 2.4.21, JDK 25 for the Gradle daemon (`gradle/gradle-daemon-jvm.properties`; bytecode targets Java 17), compileSdk 37.2, targetSdk 36, minSdk 26. Versions live in `gradle/libs.versions.toml`. Needs a local `local.properties` with `sdk.dir` (git-ignored).

## Commands

```
./gradlew :securekeys:assembleDebug            # applicationId com.dexetra.securekeys.debug
./gradlew :securekeys:testDebugUnitTest        # JVM unit tests
./gradlew :securekeys:testDebugUnitTest --tests '*PasswordGeneratorTest'
./gradlew :securekeys:lintDebug
./gradlew :securekeys:assembleRelease          # R8 + resource shrinking; signed only if secure_keystore.properties exists
```

`secure_keystore.properties` (repo root, git-ignored) keys: `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## Architecture

- `PasswordGenerator` is the core: deterministic passphrase → password (MD5-seeded, rotation-based). It is a port of the old closed-source `SecureKeysLib.aar`, and output must stay byte-identical so users regenerate the same passwords. `src/test/resources/golden.tsv` holds 192 vectors captured from the original AAR; `PasswordGeneratorTest` pins them. Do not change the algorithm (including MD5, space-stripping, rotation) without understanding that this breaks existing users' passwords.
- UI is Compose only: `MainActivity` (splash, edge-to-edge, theme override, clears clipboard on finish) → `ui/SecureKeysApp` switches between `GeneratorScreen`, `HelpScreen` and `SettingsScreen` with a saveable enum (no nav library).
- Splash: `Theme.SecureKeys.Starting` (androidx core-splashscreen; native API on 12+, compat below) shows the launcher's brass key on teal (steel at night), then `installSplashScreen()` swaps to `Theme.SecureKeys`.
- `SettingsScreen` holds the theme choice (System/Light/Dark) and About: version, developer details from `res/values/developer.xml` (name, email and website are placeholders until filled in), licenses. State lives in `GeneratorViewModel` (survives rotation; deliberately never persisted).
- Theme: Light/dark follow the system unless the user picked Light or Dark in Settings (`ThemeMode`); the override is stored in the same SharedPreferences file/key as the legacy app (`<package>_preferences`, `current_theme` = LIGHT|DARK).
- Help is native Compose text (strings in `res/values/strings.xml`); the old WebView/HTML help and its remote-script dependencies are gone.
- Brand: a key-cutting bench. Enamel teal for controls, brass for the result, nickel (light) / blued steel (dark) backgrounds. Colors, type scale and shapes live in `ui/Theme.kt`; the key's own colors are `LocalKeyColors`.
- Fonts are bundled variable TTFs in `res/font` (latin subset, OFL; licenses in `third_party/fonts/`): Archivo for UI text, Archivo at width 125 for the wordmark/headings/numerals, Martian Mono only for the generated password.
- The result is drawn by `ui/KeyBlade.kt`: a brass key whose cuts come from the password characters (`bitting()`), with the password stamped on the blade. Purely visual; it never feeds back into generation.
- Launcher icon is adaptive vector: brass key on teal, plus a monochrome layer.

## Notes

- The Gradle wrapper download can fail in restricted sandboxes; a manually unpacked Gradle 9.9.2 works for builds.
- No instrumented/UI tests exist. The emulator in this setup is software-rendered and very slow; prefer unit tests.
- CI: `.github/workflows/ci.yml` runs `testDebugUnitTest` and `lintDebug` on every push and pull request (JDK 25, ubuntu-latest); test and lint reports are uploaded as the `reports` artifact.
