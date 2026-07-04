#!/usr/bin/env ruby
# frozen_string_literal: true

# Wires the "ChefMateWatch" watchOS app target into iosApp/iosApp.xcodeproj.
#
# Idempotent: if the target already exists it aborts without changes. Re-run after `bundle install`
# so the `xcodeproj` gem (a fastlane dependency) is available:
#
#   bundle exec ruby scripts/add_watch_target.rb
#
# The watch app is pure native Swift + WatchConnectivity — the iPhone is the source of truth and
# bridges grocery data over WCSession, so there is NO Kotlin/Supabase on the watch (which is what
# lets it build for the watch device arm64 slice that supabase-kt doesn't ship).

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
  bs['OTHER_LDFLAGS'] = ['$(inherited)']
  bs['LD_RUNPATH_SEARCH_PATHS'] = ['$(inherited)', '@executable_path/Frameworks']
  bs['SKIP_INSTALL'] = 'YES' # embedded in the iOS app; must not be a top-level archive product
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

# --- shared schemes -----------------------------------------------------------------------------
# Adding the watch scheme makes it the project's first *shared* scheme, which disables Xcode's
# on-demand generation of the (unshared) iosApp / ShareExtension schemes. In CI (fresh checkout,
# no user schemes) `fastlane build_app --scheme iosApp` would then fail to find it and fall back to
# the only scheme present. So share every runnable target's scheme.
{ watch => true, ios_target => true }.each do |target, launch|
  scheme = Xcodeproj::XCScheme.new
  scheme.add_build_target(target)
  scheme.set_launch_target(target) if launch
  scheme.save_as(PROJECT_PATH, target.name, true)
end
# ShareExtension can't launch standalone — a build-only shared scheme is enough.
share_ext = project.targets.find { |t| t.name == 'ShareExtension' }
if share_ext
  scheme = Xcodeproj::XCScheme.new
  scheme.add_build_target(share_ext)
  scheme.save_as(PROJECT_PATH, share_ext.name, true)
end

project.save
puts "Added '#{TARGET_NAME}' watchOS target and embedded it in the iOS app."
