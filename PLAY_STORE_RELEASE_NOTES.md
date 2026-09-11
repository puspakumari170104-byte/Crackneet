# Play Store Release Preparation

Current package: com.crackneet.app
Current version target: 1.0.1 / versionCode 2
Target API requirement for new apps and updates from Aug 31, 2026: API 36.

## Required before production
- Generate a signed release AAB.
- Configure Play App Signing.
- Keep upload keystore and passwords private.
- Upload AAB to Play Console internal testing.
- Complete store listing, app content, content rating and Data Safety.
- Publish a publicly accessible privacy-policy URL.
- Test the release build on real devices.
- Verify all question content and rights.

## Signing
Do not commit a keystore, private key, or passwords to this repository. Use protected CI secrets when configuring signing.
