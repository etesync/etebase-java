#!/usr/bin/env bash

set -euo pipefail

VERSION=1.0.22
URL=https://github.com/jedisct1/libsodium/releases/download/$VERSION-RELEASE
TARBALL_SHA256=adbdd8f16149e81ac6078a03aca6fc03b592b89ef7b5ed83841c086191be3349
MSVC_SHA256=3e03a726fac4bc09cb61d8f29d658ef7a5eca0811de59082130414f7ca2e4279

target=$1
exec 3>&1 1>&2
cd "$(dirname "$0")"
out="$(pwd)/target/$target/libsodium"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
rm -rf "$out"
mkdir -p "$out"

verify() {
    if command -v shasum >/dev/null; then
        actual=$(shasum -a 256 "$2")
    else
        actual=$(sha256sum "$2")
    fi
    actual=${actual%% *}
    if [ "$actual" != "$1" ]; then
        echo "$2: expected sha256 $1, got $actual" >&2
        exit 1
    fi
}

case $target in
    *-pc-windows-msvc)
        case $target in
            x86_64-*) arch=x64 ;;
            aarch64-*) arch=ARM64 ;;
            *) echo "unsupported target $target" >&2; exit 1 ;;
        esac
        curl -fsSL --retry 5 --retry-all-errors -o "$work/libsodium.zip" "$URL/libsodium-$VERSION-msvc.zip"
        verify $MSVC_SHA256 "$work/libsodium.zip"
        lib="libsodium/$arch/Release/v143/static/libsodium.lib"
        (cd "$work" && "$(cygpath "$SYSTEMROOT")/System32/tar.exe" -xf libsodium.zip "$lib")
        cp "$work/$lib" "$out/"
        out=$(cygpath -m "$out")
        ;;
    *)
        curl -fsSL --retry 5 --retry-all-errors -o "$work/libsodium.tar.gz" "$URL/libsodium-$VERSION.tar.gz"
        verify $TARBALL_SHA256 "$work/libsodium.tar.gz"
        tar -xzf "$work/libsodium.tar.gz" -C "$work"
        case $target in
            *-apple-darwin)
                arch=${target%%-*}
                export CC="clang -arch ${arch/aarch64/arm64} -mmacosx-version-min=$(rustc --print deployment-target --target "$target" | cut -d= -f2)"
                ;;
        esac
        cd "$work/libsodium-$VERSION"
        ./configure --host="$target" --prefix="$work/install" --disable-shared --with-pic --disable-dependency-tracking
        jobs=$(getconf _NPROCESSORS_ONLN)
        make -j"$jobs"
        if [ "$target" = "$(rustc -vV | sed -n 's/^host: //p')" ]; then
            make -j"$jobs" check
        fi
        make install
        cp "$work/install/lib/libsodium.a" "$out/"
        ;;
esac

echo "$out" >&3
