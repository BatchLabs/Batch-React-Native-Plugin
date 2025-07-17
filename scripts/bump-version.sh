#!/bin/sh

# Check if a new version is provided
if [ -z "$1" ]; then
  echo "Error: No version specified."
  echo "Usage: $0 <new_version>"
  exit 1
fi

NEW_VERSION="$1"
OLD_VERSION_PATTERN_CORE="[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*"

echo "Attempting to update version to: $NEW_VERSION"

# --- package.json ---
# Using -E for extended regex on macOS for '+' or use [0-9][0-9]* for POSIX basic regex
# sed -i -E '' "s/\"version\": \"[0-9]+\.[0-9]+\.[0-9]+\"/\"version\": \"$NEW_VERSION\"/" ./package.json
# Alternative for wider compatibility (POSIX BRE):
sed -i '' "s/\"version\": \"${OLD_VERSION_PATTERN_CORE}\"/\"version\": \"$NEW_VERSION\"/" ./package.json
echo "Checked/Updated package.json"

# --- iOS: RNBatch.h ---
# sed -i -E '' "s/ReactNative\/[0-9]+\.[0-9]+\.[0-9]+/ReactNative\/$NEW_VERSION/" ./ios/RNBatch.h
# Alternative for wider compatibility (POSIX BRE):
sed -i '' "s/ReactNative\/${OLD_VERSION_PATTERN_CORE}/ReactNative\/$NEW_VERSION/" ./ios/RNBatch.h
echo "Checked/Updated ./ios/RNBatch.h"

# --- Android: RNBatchModuleImpl.java ---
# sed -i -E '' "s/ReactNative\/[0-9]+\.[0-9]+\.[0-9]+/ReactNative\/$NEW_VERSION/" ./android/src/main/java/com/batch/batch_rn/RNBatchModuleImpl.java
# Alternative for wider compatibility (POSIX BRE):
sed -i '' "s/ReactNative\/${OLD_VERSION_PATTERN_CORE}/ReactNative\/$NEW_VERSION/" ./android/src/main/java/com/batch/batch_rn/RNBatchModuleImpl.java
echo "Checked/Updated ./android/src/main/java/com/batch/batch_rn/RNBatchModuleImpl.java"

# --- CHANGELOG.md ---
sed -i '' "s/UPCOMING/$NEW_VERSION/" ./CHANGELOG.md
echo "Checked/Updated ./CHANGELOG.md"

echo "Version update process complete. Please verify the changes."
