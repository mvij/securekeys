# SecureKeys

[![CI](https://github.com/mvij/securekeys/actions/workflows/ci.yml/badge.svg)](https://github.com/mvij/securekeys/actions/workflows/ci.yml)

SecureKeys is an Android app that turns a simple passphrase into a strong, repeatable password. You type a passphrase, choose a length from 5 to 25 characters, and pick which extras to mix in (uppercase letters, numbers, special characters). The same passphrase with the same options always gives the same password, so nothing is ever stored: no database, no sync, no account.

## Features

- Deterministic passwords: same passphrase and options, same password, on any device running the app.
- Length 5 to 25, with optional mixed case, numbers and special characters.
- Tap the result to copy it; the clipboard is cleared when you close the app.
- Nothing is persisted. The passphrase and password live only in memory (they survive a screen rotation, nothing more), and app backup is disabled.
- Material 3 UI with light and dark themes that follow the system, plus a manual toggle.
- Built-in help screen.

## How it works

`PasswordGenerator` derives each character from an MD5 hash of the (space-stripped, lowercased) passphrase, then rotates the result. Earlier versions shipped this logic in a closed-source library, `SecureKeysLib.aar`. Version 4.0 replaces it with a Kotlin port whose output is byte-identical: `src/test/resources/golden.tsv` holds 192 test vectors captured from the original library, and `PasswordGeneratorTest` checks every one.

**Do not change the algorithm.** Any change, including to how spaces or case are handled, changes the passwords existing users get from their passphrases.

## Tech stack

- Kotlin 2.4 and Jetpack Compose (Material 3), single module `:securekeys`
- Android Gradle Plugin 9.5, Gradle 9.9 (wrapper)
- minSdk 26, targetSdk 36, compileSdk 37.2
- Dependency versions live in `gradle/libs.versions.toml`

Version 4.0 is a full rewrite of the original 2015-16 Java/XML app.

## Building

Requirements:

- JDK 17 or newer
- Android SDK, with its path set in a `local.properties` file at the repo root (git-ignored):

  ```
  sdk.dir=/path/to/Android/sdk
  ```

Then:

```
./gradlew :securekeys:assembleDebug     # debug APK, applicationId com.dexetra.securekeys.debug
./gradlew :securekeys:assembleRelease   # release APK, minified with R8 and resource shrinking
```

The debug build uses its own application ID, so it can be installed next to the release app.

### Release signing

Release builds are signed only when a `secure_keystore.properties` file exists at the repo root. Without it, `assembleRelease` still works but produces an unsigned APK. The file is git-ignored (as are `*.jks` and `*.keystore`) and must never be committed:

```
STORE_FILE=path/to/release.jks
STORE_PASSWORD=...
KEY_ALIAS=...
KEY_PASSWORD=...
```

`STORE_FILE` is resolved relative to the repo root.

## Testing

```
./gradlew :securekeys:testDebugUnitTest                               # all JVM unit tests
./gradlew :securekeys:testDebugUnitTest --tests '*PasswordGeneratorTest'
./gradlew :securekeys:lintDebug                                       # Android lint
```

The unit tests run on the JVM, no device needed. There are no instrumented or UI tests yet.

## Continuous integration

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on every push and pull request. On `ubuntu-latest` with JDK 17 it runs the unit tests and then lint (lint still runs if the tests fail), and uploads the test and lint reports as a `reports` artifact on the workflow run.

## Project layout

```
securekeys/src/main/kotlin/com/dexetra/securekeys/
  PasswordGenerator.kt     passphrase -> password algorithm
  GeneratorViewModel.kt    screen state (in memory only)
  MainActivity.kt          entry point, theme, clipboard clearing
  ui/                      Compose screens: generator, help, theme
securekeys/src/test/       unit tests and golden vectors
```
