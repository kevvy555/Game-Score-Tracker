# Android release signing

Game Score Tracker uses one permanent Android signing certificate for release APKs.

## Permanent certificate

- Alias: `qwirkle`
- Type: PKCS12
- Algorithm: RSA 4096
- SHA-256 fingerprint:
  `B8:A0:FA:E6:01:6D:BC:A0:60:B6:F9:27:8F:D1:F6:56:13:66:05:31:47:4F:8C:6F:8E:6C:9D:E3:4C:11:C4:76`

CI fails certificate verification if a release APK is ever signed by another key.

## Required GitHub Actions secrets

Configure these repository secrets:

- `ANDROID_KEYSTORE_BASE64` — base64 of the permanent PKCS12 keystore.
- `ANDROID_KEYSTORE_PASSWORD` — password for both the keystore and key.

The private keystore and password must never be committed to this repository.

## Build behaviour

- Pull requests run unit tests and build a debug APK.
- Pushes to `main` and manual workflow runs build a signed release APK when the two signing secrets are present.
- Signed release artifacts are verified against the permanent certificate fingerprint before upload.

Keep an offline backup of the keystore and password. Losing the signing key means future APKs cannot upgrade existing installations that were signed by it.
