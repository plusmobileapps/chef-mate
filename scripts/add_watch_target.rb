#!/usr/bin/env ruby
# frozen_string_literal: true

# Wires the "ChefMateWatch" watchOS app target into iosApp/iosApp.xcodeproj.
#
# Idempotent: if the target already exists it aborts without changes. Re-run after `bundle install`
# so the `xcodeproj` gem (a fastlane dependency) is available:
#
#   bundle exec ruby scripts/add_watch_target.rb
#
# Mirrors the iOS app's KMP integration: a "Compile Kotlin Framework" run-script phase builds and
# embeds the WatchShared framework via Gradle, and `import WatchShared` auto-links it (static
# framework, same as ComposeApp on the phone).

require 'xcodeproj'

PROJECT_PATH = File.expand_path('../iosApp/iosApp.xcodeproj', __dir__)
WATCH_DIR = 'ChefMateWatch' # relative to SRCROOT (iosApp/)
TARGET_NAME = 'ChefMateWatch'
WATCH_BUNDLE_ID = 'com.plusmobileapps.chefmate.ChefMate.watchkitapp'
IOS_BUNDLE_ID = 'com.plusmobileapps.chefmate.ChefMate'
TEAM_ID = 'Y89258SF5P'
DEPLOYMENT_TARGET = '10.0'

project = Xcodeproj::Project.open(PROJECT_PATH)

if project.targets.any? { |t| t.name == TARGET_NAME }
  abort("Target '#{TARGET_NAME}' already exists — nothing to do.")
end

ios_target = project.targets.find { |t| t.name == 'iosApp' }
abort('Could not find the iosApp target.') unless ios_target

# --- watchOS app target -------------------------------------------------------------------------
watch = project.new_target(:application, TARGET_NAME, :watchos, DEPLOYMENT_TARGET, nil, :swift)

# Group + source files (explicit refs — the folder is not a synchronized group).
group = project.main_group.new_group(TARGET_NAME, WATCH_DIR)
Dir.glob(File.join(__dir__, '..', 'iosApp', WATCH_DIR, '*.swift')).sort.each do |file|
  ref = group.new_reference(File.basename(file))
  watch.add_file_references([ref])
end
assets = group.new_reference('Assets.xcassets')
watch.resources_build_phase.add_file_reference(assets)
group.new_reference('Info.plist') # referenced via INFOPLIST_FILE, not a build phase

# "Compile Kotlin Framework" run-script phase, run first (mirrors the iOS app).
kotlin_phase = watch.new_shell_script_build_phase('Compile Kotlin Framework')
kotlin_phase.shell_script = <<~SH
  if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
    echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \\"YES\\""
    exit 0
  fi
  cd "$SRCROOT/.."
  if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
  fi
  ./gradlew :client:watchShared:embedAndSignAppleFrameworkForXcode
SH
kotlin_phase.always_out_of_date = '1'
watch.build_phases.delete(kotlin_phase)
watch.build_phases.insert(0, kotlin_phase)

# Build settings.
watch.build_configurations.each do |config|
  bs = config.build_settings
  bs['PRODUCT_BUNDLE_IDENTIFIER'] = WATCH_BUNDLE_ID
  # Distinct product name from the phone app ("Chef Mate.app") so both targets don't produce the
  # same bundle. The user-visible name comes from CFBundleDisplayName in Info.plist.
  bs['PRODUCT_NAME'] = 'ChefMateWatch'
  bs['SDKROOT'] = 'watchos'
  bs['WATCHOS_DEPLOYMENT_TARGET'] = DEPLOYMENT_TARGET
  bs['TARGETED_DEVICE_FAMILY'] = '4'
  bs['SWIFT_VERSION'] = '5.0'
  bs['GENERATE_INFOPLIST_FILE'] = 'NO'
  bs['INFOPLIST_FILE'] = "#{WATCH_DIR}/Info.plist"
  bs['ASSETCATALOG_COMPILER_APPICON_NAME'] = 'AppIcon'
  bs['DEVELOPMENT_TEAM'] = TEAM_ID
  bs['CURRENT_PROJECT_VERSION'] = '1'
  bs['MARKETING_VERSION'] = '1.0.0'
  bs['OTHER_LDFLAGS'] = ['$(inherited)', '-lsqlite3']
  bs['LD_RUNPATH_SEARCH_PATHS'] = ['$(inherited)', '@executable_path/Frameworks']
  bs['SKIP_INSTALL'] = 'NO'
  bs['ENABLE_PREVIEWS'] = 'YES'
  bs['SWIFT_EMIT_LOC_STRINGS'] = 'YES'

  if config.name == 'Release'
    # Manual signing via Fastlane match for release/App Store archives (the watch is embedded and
    # signed during the iOS app archive), mirroring the iOS app + ShareExtension targets. Requires
    # the watch bundle id in fastlane/Matchfile + the release lane's provisioningProfiles.
    bs['CODE_SIGN_STYLE'] = 'Manual'
    bs['CODE_SIGN_IDENTITY'] = 'Apple Development'
    bs['CODE_SIGN_IDENTITY[sdk=watchos*]'] = 'Apple Distribution'
    bs['PROVISIONING_PROFILE_SPECIFIER[sdk=watchos*]'] = "match AppStore #{WATCH_BUNDLE_ID}"
  else
    # Automatic signing keeps local device/simulator dev builds friction-free.
    bs['CODE_SIGN_STYLE'] = 'Automatic'
  end
end

# --- embed the watch app into the iOS app -------------------------------------------------------
ios_target.add_dependency(watch)
embed = ios_target.new_copy_files_build_phase('Embed Watch Content')
embed.dst_subfolder_spec = '16' # $(CONTENTS_FOLDER_PATH)-relative
embed.dst_path = '$(CONTENTS_FOLDER_PATH)/Watch'
build_file = embed.add_file_reference(watch.product_reference)
build_file.settings = { 'ATTRIBUTES' => ['RemoveHeadersOnCopy'] }

# --- shared scheme so `xcodebuild -scheme ChefMateWatch` works ----------------------------------
scheme = Xcodeproj::XCScheme.new
scheme.add_build_target(watch)
scheme.set_launch_target(watch)
scheme.save_as(PROJECT_PATH, TARGET_NAME, true)

project.save
puts "Added '#{TARGET_NAME}' watchOS target and embedded it in the iOS app."
