#!/usr/bin/env bash
# Dry-run the version bump logic used by .github/workflows/bump-version.yml.
# Creates temporary copies of the version files, applies the same sed/awk
# transformations, and verifies all expected values are written correctly.
# Exits non-zero if any check fails.
#
# Usage:
#   ./scripts/test-version-bump.sh                  # default: 0.99.0 build 99
#   ./scripts/test-version-bump.sh 1.2.3 42         # custom version and build
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
GRADLE_FILE="${ROOT_DIR}/client/composeApp/build.gradle.kts"
XCCONFIG_FILE="${ROOT_DIR}/iosApp/Configuration/Config.xcconfig"

# ── Platform-specific commands ───────────────────────────────────────────────
# CI runs on Linux (uses sed -i and tac).
# Local development typically runs on macOS (uses sed -i '' and tail -r).
if [[ "$(uname)" == "Darwin" ]]; then
    sed_inplace() { sed -i '' "$@"; }
    reverse() { tail -r "$@"; }
else
    sed_inplace() { sed -i "$@"; }
    reverse() { tac "$@"; }
fi

# ── Arguments ────────────────────────────────────────────────────────────────
TEST_VERSION="${1:-0.99.0}"
TEST_BUILD="${2:-99}"

if [[ ! "${TEST_VERSION}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: '${TEST_VERSION}' is not a valid semver (expected X.Y.Z)" >&2
    exit 1
fi

# ── macOS version mapping ─────────────────────────────────────────────────────
# macOS DMG packaging requires MAJOR > 0; map 0.x.y → 1.x.y.
# See comment in client/composeApp/build.gradle.kts macOS block.
IFS='.' read -r V_MAJOR V_MINOR V_PATCH <<< "${TEST_VERSION}"
if [[ "${V_MAJOR}" == "0" ]]; then
    EXPECTED_MAC_VERSION="1.${V_MINOR}.${V_PATCH}"
else
    EXPECTED_MAC_VERSION="${TEST_VERSION}"
fi

# ── Temp workspace ───────────────────────────────────────────────────────────
TMPDIR_WORK="$(mktemp -d)"
trap 'rm -rf "${TMPDIR_WORK}"' EXIT

GRADLE_COPY="${TMPDIR_WORK}/build.gradle.kts"
XCCONFIG_COPY="${TMPDIR_WORK}/Config.xcconfig"

cp "${GRADLE_FILE}" "${GRADLE_COPY}"
cp "${XCCONFIG_FILE}" "${XCCONFIG_COPY}"

# ── Apply transformations (mirrors bump-version.yml "Apply version bump" step) ─

# Android versionCode
sed_inplace "s/versionCode = [0-9]*/versionCode = ${TEST_BUILD}/" "${GRADLE_COPY}"

# Android versionName
sed_inplace "s/versionName = \"[^\"]*\"/versionName = \"${TEST_VERSION}\"/" "${GRADLE_COPY}"

# Desktop packageVersion — first occurrence (generic/JVM)
awk -v new="${TEST_VERSION}" '
    /packageVersion[[:space:]]*=/ && !done {
        sub(/packageVersion = "[^"]*"/, "packageVersion = \"" new "\"")
        done=1
    }
    1' "${GRADLE_COPY}" > "${GRADLE_COPY}.tmp" && mv "${GRADLE_COPY}.tmp" "${GRADLE_COPY}"

# Desktop macOS packageVersion — second (last) occurrence
reverse "${GRADLE_COPY}" | awk -v new="${EXPECTED_MAC_VERSION}" '
    /packageVersion[[:space:]]*=/ && !done {
        sub(/packageVersion = "[^"]*"/, "packageVersion = \"" new "\"")
        done=1
    }
    1' | reverse > "${GRADLE_COPY}.tmp" && mv "${GRADLE_COPY}.tmp" "${GRADLE_COPY}"

# iOS build number and marketing version
sed_inplace "s/CURRENT_PROJECT_VERSION=.*/CURRENT_PROJECT_VERSION=${TEST_BUILD}/" "${XCCONFIG_COPY}"
sed_inplace "s/MARKETING_VERSION=.*/MARKETING_VERSION=${TEST_VERSION}/" "${XCCONFIG_COPY}"

# ── Verification ─────────────────────────────────────────────────────────────
FAIL=0

check() {
    local label="${1}"
    local expected="${2}"
    local actual="${3}"
    if [[ "${actual}" == "${expected}" ]]; then
        printf "  \033[0;32m✓\033[0m %-42s %s\n" "${label}" "${actual}"
    else
        printf "  \033[0;31m✗\033[0m %-42s expected '%s', got '%s'\n" \
            "${label}" "${expected}" "${actual}" >&2
        FAIL=1
    fi
}

ACTUAL_VCODE=$(sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' "${GRADLE_COPY}")
ACTUAL_VNAME=$(sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' "${GRADLE_COPY}")
ACTUAL_PKG_VERSIONS=$(sed -n 's/.*packageVersion = "\([^"]*\)".*/\1/p' "${GRADLE_COPY}")
ACTUAL_PKG_FIRST=$(printf '%s\n' "${ACTUAL_PKG_VERSIONS}" | head -1)
ACTUAL_PKG_LAST=$(printf '%s\n' "${ACTUAL_PKG_VERSIONS}" | tail -1)
ACTUAL_IOS_BUILD=$(sed -n 's/CURRENT_PROJECT_VERSION=\(.*\)/\1/p' "${XCCONFIG_COPY}")
ACTUAL_IOS_VERSION=$(sed -n 's/MARKETING_VERSION=\(.*\)/\1/p' "${XCCONFIG_COPY}")

printf '\nTest: bump to version \033[1m%s\033[0m (build \033[1m%s\033[0m)\n\n' \
    "${TEST_VERSION}" "${TEST_BUILD}"

printf "Checking %s:\n" "${GRADLE_COPY}"
check "versionCode" "${TEST_BUILD}" "${ACTUAL_VCODE}"
check "versionName" "${TEST_VERSION}" "${ACTUAL_VNAME}"
check "packageVersion (JVM/generic, first occurrence)" "${TEST_VERSION}" "${ACTUAL_PKG_FIRST}"
check "packageVersion (macOS, last occurrence)" "${EXPECTED_MAC_VERSION}" "${ACTUAL_PKG_LAST}"

printf "\nChecking %s:\n" "${XCCONFIG_COPY}"
check "CURRENT_PROJECT_VERSION" "${TEST_BUILD}" "${ACTUAL_IOS_BUILD}"
check "MARKETING_VERSION" "${TEST_VERSION}" "${ACTUAL_IOS_VERSION}"

printf '\n'
if [[ "${FAIL}" -ne 0 ]]; then
    printf '\033[0;31mFAILED: one or more checks did not match.\033[0m\n' >&2
    exit 1
fi
printf '\033[0;32mAll checks passed.\033[0m\n'
