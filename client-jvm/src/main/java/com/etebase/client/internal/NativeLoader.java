package com.etebase.client.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class NativeLoader {
    private static final String LIB_NAME = "etebase_jni";
    private static final String OVERRIDE_PROPERTY = "com.etebase.client.native.path";

    private static final Object LOCK = new Object();
    private static volatile boolean loaded = false;

    private NativeLoader() {
    }

    public static void load() {
        if (loaded) return;
        synchronized (LOCK) {
            if (loaded) return;
            String override = System.getProperty(OVERRIDE_PROPERTY);
            if (override != null && !override.isEmpty()) {
                System.load(override);
            } else if (!loadFromLibraryPath()) {
                loadFromClasspath();
            }
            loaded = true;
        }
    }

    private static boolean loadFromLibraryPath() {
        try {
            System.loadLibrary(LIB_NAME);
            return true;
        } catch (UnsatisfiedLinkError ignored) {
            return false;
        }
    }

    private static void loadFromClasspath() {
        String os = detectOs();
        String arch = detectArch();
        String libFile = libFileFor(os);
        String resource = "/com/etebase/client/native/" + os + "-" + arch + "/" + libFile;

        try (InputStream in = NativeLoader.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new UnsatisfiedLinkError(
                    "Etebase native library not found on classpath: " + resource
                    + " (detected os=" + os + " arch=" + arch + "). "
                    + "Either the com.etebase:client-jvm artifact is missing a native binary for this platform, "
                    + "or set -D" + OVERRIDE_PROPERTY + "=/absolute/path/to/lib to override."
                );
            }
            Path tmp = Files.createTempFile(LIB_NAME + "-", "-" + libFile);
            tmp.toFile().deleteOnExit();
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            System.load(tmp.toAbsolutePath().toString());
        } catch (IOException e) {
            UnsatisfiedLinkError err = new UnsatisfiedLinkError("Failed to extract Etebase native library from " + resource);
            err.initCause(e);
            throw err;
        }
    }

    private static String detectOs() {
        String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (name.contains("mac") || name.contains("darwin")) return "macos";
        if (name.contains("win")) return "windows";
        if (name.contains("nux") || name.contains("nix")) return "linux";
        throw new UnsatisfiedLinkError("Unsupported OS for Etebase: " + name);
    }

    private static String detectArch() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (arch.equals("amd64") || arch.equals("x86_64")) return "x86_64";
        if (arch.equals("aarch64") || arch.equals("arm64")) return "aarch64";
        throw new UnsatisfiedLinkError("Unsupported CPU architecture for Etebase: " + arch);
    }

    private static String libFileFor(String os) {
        switch (os) {
            case "macos": return "lib" + LIB_NAME + ".dylib";
            case "linux": return "lib" + LIB_NAME + ".so";
            case "windows": return LIB_NAME + ".dll";
            default: throw new AssertionError(os);
        }
    }
}
