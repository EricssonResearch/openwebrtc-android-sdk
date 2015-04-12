# [WIP] OpenWebRTC Android SDK

SDK for adding OpenWebRTC to your Android app

## Binaries

##### *** This doesn't work yet ***
Add `compile 'io.openwebrtc:openwebrtc-android-sdk:0.1'` to your gradle dependencies.

## Development

To build the SDK as a part of another project, clone `openwebrtc-android-sdk` to your project's root folder, and add it to your settings.gradle:
```
include ':app', ':openwebrtc-android-sdk'
project(':openwebrtc-android-sdk').projectDir = new File('openwebrtc-android-sdk/sdk')
```

Then simply add `compile project(':openwebrtc-android-sdk')` to your module's dependencies.

Also make sure to copy or link `cerbero/dist/android_armv7/lib/jni/openwebrtc.jar` to `openwebrtc-android-sdk/sdk/libs/openwebrtc.jar`
