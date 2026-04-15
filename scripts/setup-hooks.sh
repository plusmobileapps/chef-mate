#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOOK_SRC="$REPO_ROOT/scripts/pre-commit"
HOOK_DST="$REPO_ROOT/.git/hooks/pre-commit"

# Install ktfmt if not present
if ! command -v ktfmt &> /dev/null; then
    echo "ktfmt not found. Attempting to install..."
    if command -v brew &> /dev/null; then
        brew install ktfmt
    elif command -v sdkman &> /dev/null; then
        sdk install ktfmt
    else
        echo "Error: Could not install ktfmt automatically."
        echo "Please install it manually: https://github.com/facebook/ktfmt"
        exit 1
    fi
fi

# Install pre-commit hook
cp "$HOOK_SRC" "$HOOK_DST"
chmod +x "$HOOK_DST"

echo "Done! ktfmt pre-commit hook installed."
