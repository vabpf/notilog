#!/usr/bin/env fish

# Remove old adb path
set -gx PATH (string match -v "/home/vab/android/platform-tools" $PATH)

# Add Windows adb first
set -gx PATH /mnt/d/Apps/Android/Sdk/platform-tools $PATH

echo "Using Windows adb:"
which adb