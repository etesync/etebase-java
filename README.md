<p align="center">
  <img width="120" src="https://github.com/etesync/etesync-web/blob/master/src/images/logo.svg" />
  <h1 align="center">Etebase - Encrypt Everything</h1>
</p>

A Java/Android library for Etebase

This package is implemented in Rust and exposes a Java API for people to use.

![GitHub tag](https://img.shields.io/github/tag/etesync/etesync-java.svg)
[![Chat on freenode](https://img.shields.io/badge/irc.freenode.net-%23EteSync-blue.svg)](https://webchat.freenode.net/?channels=#etesync)

# Build

Make sure you have the Android NDK in your build path:

```
export PATH="$PATH:/opt/android-sdk/ndk/21.3.6528147/toolchains/llvm/prebuilt/linux-x86_64/bin/"
```

Add relevant toolchains:

```
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

Start the build

```
./build.sh
```
