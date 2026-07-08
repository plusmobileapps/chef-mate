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

Install the Developer ID cert into the keychain for desktop signing.

Read-only by default (CI). Run once locally with `readonly:false` to generate & store the cert.

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
