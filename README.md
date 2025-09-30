# first_project

A simple Java desktop app (feed viewer) plus a React Native starter app for Android/iOS.

## React Native app: MyAndroidApp
- Purpose: Mobile starter scaffold to build out features (feed list, post details, image cache) mirroring the desktop app in the future.
- Tech: React Native 0.81, TypeScript, Metro, Jest.

### Run (Android)
```bash
cd MyAndroidApp
npm install
# Start Metro bundler
npm start
# In a separate terminal, build & run Android (emulator running or device connected)
npm run android
```

### Run (iOS) – optional on macOS
```bash
cd MyAndroidApp
npm install
# Install pods once
cd ios && pod install && cd ..
# Start Metro
npm start
# Build & run iOS
npm run ios
```

### Scripts
- `npm start`: start Metro bundler
- `npm run android`: build and run on Android
- `npm run ios`: build and run on iOS (CocoaPods required)
- `npm test`: unit tests

### Next steps
- Implement feed list screen and post detail screen
- Add image caching similar to desktop `ImageCache`
- Set up basic navigation and state management 