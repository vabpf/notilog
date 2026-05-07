#!/usr/bin/env fish

# Remove Windows adb path
set -gx PATH (string match -v "/mnt/d/Apps/Android/Sdk/platform-tools" $PATH)

# Restore old adb path to front
set -gx PATH /home/vab/android/platform-tools $PATH

echo "Restored old adb:"
which adb