# SecureKeys

Android app that generates strong, reproducible passwords from a simple passphrase: pick a length (5-25) and which character classes to include. The same passphrase and options always give the same password, so nothing needs to be stored.

## Build

Requires JDK 17+ and the Android SDK (set `sdk.dir` in `local.properties`).

```
./gradlew :securekeys:assembleDebug
./gradlew :securekeys:testDebugUnitTest
```

Release signing is optional: create `secure_keystore.properties` at the repo root with `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
