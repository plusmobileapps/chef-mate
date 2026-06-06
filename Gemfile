source "https://rubygems.org"

gem "fastlane"
# representable (a transitive fastlane dependency) requires multi_json at load
# time but only declares it as a soft dependency, so it never lands in the
# bundle on its own. Declaring it explicitly keeps `fastlane` loadable.
gem "multi_json"
