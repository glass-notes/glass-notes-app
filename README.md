# glass-notes-app

## Demo

[![Glass Notes demo YouTube video](https://img.youtube.com/vi/X09_pJ8Hj90/0.jpg)](https://www.youtube.com/watch?v=X09_pJ8Hj90)

## Installation (for developers)

1. `git clone https://github.com/glass-notes/glass-notes-app.git`
2. Open in Android Studio
3. Run the application to your Google Glass
4. Visit GitHub's Token Settings: https://github.com/settings/tokens
5. Create a token with the `gist :: Create gists` option ticked
6. Generate an access token
7. Visit https://jsoneditoronline.org/ and add the following JSON code:
```json
{
  "savePeriodMs": 5000,
  "preferredDataStoreName": "GitHubOfflineSyncingDataStore",
  "githubAccessToken": "GITHUB_ACCESS_TOKEN"
}
```
8. Replace the access token with the one you generated above
9. Visit https://www.the-qrcode-generator.com/ and paste in the modified JSON from above
10. In the Glass Notes application, select the `Load settings →` and scan the QR code from step 9
11. Enjoy the application!
