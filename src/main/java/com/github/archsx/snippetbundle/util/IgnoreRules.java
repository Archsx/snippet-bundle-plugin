package com.github.archsx.snippetbundle.util;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class IgnoreRules {

    // Common "meaningless/huge/generated" directories
    private static final Set<String> IGNORED_DIR_NAMES = new HashSet<>(Arrays.asList(
            ".git", ".idea", ".gradle", ".mvn",
            "node_modules", "dist", "build", "out", "target",
            ".next", ".nuxt", ".cache", ".sass-cache",
            ".pytest_cache", "__pycache__",
            ".vscode"
    ));

    // Common binary/unsuitable for LLM files
    private static final Set<String> BINARY_EXTENSIONS = new HashSet<>(Arrays.asList(
            "class", "jar", "war", "zip", "7z", "rar", "tar", "gz",
            "png", "jpg", "jpeg", "gif", "webp", "bmp", "ico",
            "pdf",
            "mp3", "mp4", "wav", "avi", "mov",
            "exe", "dll", "so", "dylib"
    ));

    // Max file size (prevent freeze/clipboard overflow); can be moved to Settings later
    public static final long MAX_FILE_BYTES = 512L * 1024L; // 512KB

    private IgnoreRules() {}

    /** Whether to ignore this file/directory (based on directory name/hidden artifacts etc.) */
    public static boolean shouldIgnore(VirtualFile file) {
        if (file == null) return true;
        if (!file.isValid()) return true;

        if (file.isDirectory()) {
            String name = file.getName();
            return IGNORED_DIR_NAMES.contains(name);
        }

        // Ignore oversized files (default strategy: skip directly)
        if (file.getLength() > MAX_FILE_BYTES) return true;

        String ext = extLower(file);
        return !ext.isEmpty() && BINARY_EXTENSIONS.contains(ext);
    }

    /** Binary detection: extension + content sampling (read first 4KB) */
    public static boolean isProbablyBinary(VirtualFile file) {
        if (file == null || file.isDirectory()) return false;

        String ext = extLower(file);
        if (!ext.isEmpty() && BINARY_EXTENSIONS.contains(ext)) return true;

        int sampleSize = 4096;
        try (InputStream in = file.getInputStream()) {
            byte[] buf = new byte[sampleSize];
            int n = in.read(buf);
            if (n <= 0) return false;

            int zeros = 0;
            int weird = 0;

            for (int i = 0; i < n; i++) {
                int b = buf[i] & 0xFF;
                if (b == 0) zeros++;

                // Allow: tab(9) / lf(10) / cr(13)
                if (b < 0x09 || (b > 0x0D && b < 0x20) || b == 0x7F) {
                    weird++;
                }
            }

            if (zeros > 0) return true;
            double weirdRatio = weird / (double) n;
            return weirdRatio > 0.15;
        } catch (IOException e) {
            // If unreadable, treat as "unavailable" and process as binary to avoid polluting output
            return true;
        }
    }

    private static String extLower(VirtualFile file) {
        String ext = file.getExtension();
        return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
    }
}
