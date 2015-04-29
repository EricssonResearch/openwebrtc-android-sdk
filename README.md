# [WIP] OpenWebRTC Android SDK

SDK for adding OpenWebRTC to your Android app

## Binaries

Add `compile 'io.openwebrtc:openwebrtc-android-sdk:0.1.0'` to your gradle dependencies.

## Development

To build the SDK as a part of another project, clone `openwebrtc-android-sdk` to your project's root folder, and add it to your settings.gradle:
```
include ':app', ':openwebrtc-android-sdk'
project(':openwebrtc-android-sdk').projectDir = new File('openwebrtc-android-sdk/sdk')
```

Then simply add `compile project(':openwebrtc-android-sdk')` to your module's dependencies.

Another approach is to clone the SDK to a different directory than your app, and then point directly to that directory.
This is done in the NativeCall example in openwebrtc-examples [link](https://github.com/EricssonResearch/openwebrtc-examples/blob/master/android/NativeCall/settings.gradle)
