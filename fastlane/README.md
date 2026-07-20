fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android release

```sh
[bundle exec] fastlane android release
```

Build release AAB and upload to Play Store internal track

----


## Mac

### mac certificates

```sh
[bundle exec] fastlane mac certificates
```

Generate/refresh the Mac App Store certs + provisioning profile (read-write match).

Run once locally after creating the macOS App Store Connect record.

Pass force:true to regenerate the profile after enabling a new capability on the App ID.

### mac release

```sh
[bundle exec] fastlane mac release
```

Build the Mac App Store .pkg and upload it to App Store Connect.

----


## iOS

### ios certificates

```sh
[bundle exec] fastlane ios certificates
```

Generate/refresh signing certificates and provisioning profiles (read-write match)

Run this once after adding an app id (e.g. the watch app) so its profile is registered.

### ios release

```sh
[bundle exec] fastlane ios release
```

Build release IPA and upload to App Store Connect

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
