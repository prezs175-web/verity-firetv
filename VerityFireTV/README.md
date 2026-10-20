# Verity for Fire TV — Android APK project

A landscape Android app designed for Fire TV remotes. It includes:
- Question entry and AI answers using OpenAI's Responses API (`gpt-4.1-mini`)
- +10 coins per successfully submitted question
- Cosmetic shop with five sample cosmetics
- Local saved coins, purchases, and equipped cosmetic
- D-pad focusable controls and Leanback launcher category
- GitHub Actions workflow that builds and uploads a debug APK

## Build the APK with GitHub Actions

1. Download and unzip this project.
2. Create a new GitHub repository, for example `verity-fire-tv`.
3. On the repository page, choose **Add file → Upload files**.
4. Upload the *contents* of this unzipped folder (including `.github`, `app`, `build.gradle`, `settings.gradle`, and `gradle.properties`), then commit to the `main` branch.
   - Ensure `.github/workflows/build-apk.yml` is uploaded. GitHub may hide dot-folders in some file pickers; if needed, enable hidden files or upload the folder through Git.
5. Open the repository's **Actions** tab.
6. Select **Build Verity Fire TV APK** in the left sidebar.
7. If the workflow has not run automatically, click **Run workflow**.
8. Open the completed green run. Under **Artifacts**, click **Verity-FireTV-APK** to download a ZIP.
9. Extract that artifact ZIP. It contains `app-debug.apk`.

## Install on Fire TV

Use your preferred APK transfer method. For Downloader, host the APK at a URL you control or use a trusted file-transfer method, open Downloader on the Fire TV, enter the direct APK URL, download it, and follow Fire TV's prompt to allow installation from that source if required. Menu names differ by Fire TV OS version. Only install APKs you trust.

## Configure AI answers

1. Install and open Verity.
2. Choose **SETTINGS / API KEY**.
3. Paste an OpenAI API key you created in your OpenAI developer account, then save.
4. Type a question with the remote keyboard and select **ASK VERITY**.

AI answers require internet access, a valid API key, and API billing/limits set up on the key's account. This project does not include a key.

### Important API-key warning

This is a prototype. It stores the key in the app's local preferences and calls the API directly from the device. A key embedded in or stored by a client app can potentially be extracted. For a public release, use a private backend that keeps the API key server-side, add rate limits, and authenticate users. Never commit your API key to GitHub.

## Notes

- Coins and cosmetics are saved locally on the Fire TV device and do not sync between devices.
- The shop currently uses placeholder ASCII-style character art and sample cosmetics. It is a functional starter project, not an official Minecraft product.
- The app targets Android API 28 for broad Fire TV compatibility and compiles against Android 35.
