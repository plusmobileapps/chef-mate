#!/usr/bin/env bash
set -euo pipefail

# ── File paths ──────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
GRADLE_FILE="$ROOT_DIR/client/composeApp/build.gradle.kts"
XCCONFIG_FILE="$ROOT_DIR/iosApp/Configuration/Config.xcconfig"

# ── Helpers ─────────────────────────────────────────────────────────────
red()    { printf '\033[0;31m%s\033[0m\n' "$*"; }
green()  { printf '\033[0;32m%s\033[0m\n' "$*"; }
yellow() { printf '\033[0;33m%s\033[0m\n' "$*"; }
bold()   { printf '\033[1m%s\033[0m\n' "$*"; }

# ── Read current versions ──────────────────────────────────────────────
read_android_version_code() { sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' "$GRADLE_FILE"; }
read_android_version_name() { sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' "$GRADLE_FILE"; }

read_ios_build()   { sed -n 's/CURRENT_PROJECT_VERSION=\(.*\)/\1/p' "$XCCONFIG_FILE"; }
read_ios_version() { sed -n 's/MARKETING_VERSION=\(.*\)/\1/p' "$XCCONFIG_FILE"; }

# Desktop: first packageVersion is generic, second (inside macOS block) is macOS
read_desktop_version()     { sed -n 's/.*packageVersion = "\([^"]*\)".*/\1/p' "$GRADLE_FILE" | head -1; }
read_desktop_mac_version() { sed -n 's/.*packageVersion = "\([^"]*\)".*/\1/p' "$GRADLE_FILE" | tail -1; }

# Top-level Gradle `version = "..."` — read by Hydraulic Conveyor.
read_gradle_project_version() { sed -n 's/^version = "\([^"]*\)".*/\1/p' "$GRADLE_FILE" | head -1; }

show_current() {
    bold "Current versions:"
    echo ""
    printf "  %-18s  %-14s  %s\n" "Platform" "Version" "Build"
    printf "  %-18s  %-14s  %s\n" "────────────────" "──────────────" "─────"
    printf "  %-18s  %-14s  %s\n" "Android"           "$(read_android_version_name)" "$(read_android_version_code)"
    printf "  %-18s  %-14s  %s\n" "iOS"               "$(read_ios_version)"          "$(read_ios_build)"
    printf "  %-18s  %-14s  %s\n" "Desktop"           "$(read_desktop_version)"      "—"
    printf "  %-18s  %-14s  %s\n" "Desktop (macOS)"   "$(read_desktop_mac_version)"  "—"
    printf "  %-18s  %-14s  %s\n" "Conveyor (gradle)" "$(read_gradle_project_version)" "—"
    echo ""
}

# ── Compute macOS version ──────────────────────────────────────────────
to_mac_version() {
    echo "$1"
}

# ── Increment helpers ──────────────────────────────────────────────────
increment_build() {
    local current_code
    current_code="$(read_android_version_code)"
    local new_code=$(( current_code + 1 ))
    echo "$new_code"
}

increment_patch() {
    local ver="$1"
    local major minor patch
    IFS='.' read -r major minor patch <<< "$ver"
    echo "${major}.${minor}.$(( patch + 1 ))"
}

# ── Apply version changes ─────────────────────────────────────────────
apply_versions() {
    local new_version="$1"
    local new_build="$2"
    local new_mac_version
    new_mac_version="$(to_mac_version "$new_version")"

    # Android versionCode
    sed -i '' "s/versionCode = [0-9]*/versionCode = ${new_build}/" "$GRADLE_FILE"

    # Android versionName
    sed -i '' "s/versionName = \"[^\"]*\"/versionName = \"${new_version}\"/" "$GRADLE_FILE"

    # Top-level Gradle project version (read by Hydraulic Conveyor).
    # Anchored to start-of-line so we only match the project-level declaration,
    # not packageVersion / versionName / versionCode lines further down.
    sed -i '' "s/^version = \"[^\"]*\"/version = \"${new_version}\"/" "$GRADLE_FILE"

    # Desktop packageVersion (generic — first occurrence)
    # Use awk to only replace the first match
    awk -v new="$new_version" '
        /packageVersion[[:space:]]*=/ && !done { sub(/packageVersion = "[^"]*"/, "packageVersion = \"" new "\""); done=1 }
        1' "$GRADLE_FILE" > "$GRADLE_FILE.tmp" && mv "$GRADLE_FILE.tmp" "$GRADLE_FILE"

    # Desktop macOS packageVersion (second occurrence)
    # Replace from the end using tail -r + awk trick
    tail -r "$GRADLE_FILE" | awk -v new="$new_mac_version" '
        /packageVersion[[:space:]]*=/ && !done { sub(/packageVersion = "[^"]*"/, "packageVersion = \"" new "\""); done=1 }
        1' | tail -r > "$GRADLE_FILE.tmp" && mv "$GRADLE_FILE.tmp" "$GRADLE_FILE"

    # iOS
    sed -i '' "s/CURRENT_PROJECT_VERSION=.*/CURRENT_PROJECT_VERSION=${new_build}/" "$XCCONFIG_FILE"
    sed -i '' "s/MARKETING_VERSION=.*/MARKETING_VERSION=${new_version}/" "$XCCONFIG_FILE"
}

# ── Validate semver ───────────────────────────────────────────────────
validate_version() {
    if [[ ! "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        red "Error: '$1' is not a valid semver (expected X.Y.Z)"
        exit 1
    fi
}

# ── Usage ─────────────────────────────────────────────────────────────
usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Bump the version across Android, iOS, and Desktop clients.

Options:
  (no args)          Interactive mode — show current versions and prompt
  --build            Increment build number only, keep current version
  --version X.Y.Z    Set the semantic version (build number auto-increments)
  --build-number N   Override the build number (use with --version)
  -h, --help         Show this help message

Examples:
  $(basename "$0")                        # interactive
  $(basename "$0") --build                # 0.1.18 build 18 → 0.1.18 build 19
  $(basename "$0") --version 0.2.0       # set version to 0.2.0, build 19
  $(basename "$0") --version 1.0.0 --build-number 1   # full override
EOF
}

# ── Main ──────────────────────────────────────────────────────────────

NEW_VERSION=""
NEW_BUILD=""
BUILD_ONLY=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --build)       BUILD_ONLY=true; shift ;;
        --version)     NEW_VERSION="$2"; shift 2 ;;
        --build-number) NEW_BUILD="$2"; shift 2 ;;
        -h|--help)     usage; exit 0 ;;
        *)             red "Unknown option: $1"; usage; exit 1 ;;
    esac
done

show_current

if $BUILD_ONLY; then
    NEW_VERSION="$(read_android_version_name)"
    NEW_BUILD="$(increment_build)"
elif [[ -n "$NEW_VERSION" ]]; then
    validate_version "$NEW_VERSION"
    [[ -z "$NEW_BUILD" ]] && NEW_BUILD="$(increment_build)"
elif [[ -n "$NEW_BUILD" ]]; then
    NEW_VERSION="$(read_android_version_name)"
else
    # Interactive mode
    bold "What would you like to do?"
    echo ""
    echo "  1) Increment build number only (keep current version)"
    echo "  2) Bump patch   ($(read_android_version_name) → $(increment_patch "$(read_android_version_name)"))"
    echo "  3) Enter a custom version"
    echo "  4) Enter a custom version and build number"
    echo ""
    read -rp "Choice [1/2/3/4]: " choice

    case "$choice" in
        1)
            NEW_VERSION="$(read_android_version_name)"
            NEW_BUILD="$(increment_build)"
            ;;
        2)
            NEW_VERSION="$(increment_patch "$(read_android_version_name)")"
            NEW_BUILD="$(increment_build)"
            ;;
        3)
            read -rp "Enter new version (X.Y.Z): " NEW_VERSION
            validate_version "$NEW_VERSION"
            NEW_BUILD="$(increment_build)"
            ;;
        4)
            read -rp "Enter new version (X.Y.Z): " NEW_VERSION
            validate_version "$NEW_VERSION"
            read -rp "Enter build number: " NEW_BUILD
            [[ -z "$NEW_BUILD" ]] && { red "Build number is required"; exit 1; }
            ;;
        *)
            red "Invalid choice"; exit 1
            ;;
    esac
fi

echo ""
bold "Applying:"
echo "  Version:       $NEW_VERSION"
echo "  Build number:  $NEW_BUILD"
echo "  macOS version: $(to_mac_version "$NEW_VERSION")"
echo ""

apply_versions "$NEW_VERSION" "$NEW_BUILD"

green "Done! Updated versions:"
echo ""
show_current
