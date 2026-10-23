#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"
artifacts=$1

for abi in armeabi-v7a arm64-v8a x86 x86_64; do
    mkdir -p client/src/main/jniLibs/$abi
    cp "$artifacts/android-$abi/libetebase_jni.so" client/src/main/jniLibs/$abi/
done
mkdir -p client/src/main/java/com/etebase/client
cp "$artifacts"/android-generated-java/*.java client/src/main/java/com/etebase/client/

for subdir in macos-aarch64 macos-x86_64 linux-x86_64 linux-aarch64 windows-x86_64 windows-aarch64; do
    mkdir -p client-jvm/src/main/resources/com/etebase/client/native/$subdir
    cp "$artifacts/desktop-$subdir"/* client-jvm/src/main/resources/com/etebase/client/native/$subdir/
done
mkdir -p client-jvm/src/main/java/com/etebase/client
cp "$artifacts"/desktop-generated-java/*.java client-jvm/src/main/java/com/etebase/client/
