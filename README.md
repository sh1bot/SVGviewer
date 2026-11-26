# SVG Viewer for Android

A viewer for Android 6.0 and later. Open an SVG in your file manager and choose **SVG Viewer** from **Open with**. The image starts fitted to the screen. Pinch to zoom and drag to pan once zoomed in.

The viewer appears for files the file manager sends as `image/svg+xml` using a `content://` URI. It has no launcher icon.

## Build

Open this directory in Android Studio and sync it with Android SDK Platform 35 and JDK 17. To install from Android Studio, set the app run configuration’s **Launch** option to **Nothing**, then click **Run** with a device or emulator connected. Android Studio may ask for a Gradle installation because this project has no Gradle wrapper.

With Gradle 8.9 installed, you can also build from the project directory:

```sh
gradle :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## GitHub releases

Every push to the default branch updates the [development build](https://github.com/sh1bot/SVGviewer/releases/tag/development) prerelease. Push a tag such as `v1.0.0` to publish a separate release with its own APK. The `development` tag is reserved for the rolling prerelease. You can also run **Build and publish APK** manually from the Actions tab.

For persistent APK signing, add all four repository secrets under **Settings → Secrets and variables → Actions**:

- `ANDROID_KEYSTORE_BASE64` — the base64-encoded keystore file.
- `ANDROID_KEYSTORE_PASSWORD` — the keystore password.
- `ANDROID_KEY_ALIAS` — the signing key alias.
- `ANDROID_KEY_PASSWORD` — the signing key password.

If any signing secret is missing, the workflow uses a temporary debug key so the APK is still installable. Android requires matching signatures for in-place updates. You may need to uninstall a previous APK before installing a build signed with a different key.
