package com.github.archsx.snippetbundle.service;

import com.github.archsx.snippetbundle.model.FileTreeNode;
import com.github.archsx.snippetbundle.util.IgnoreRules;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.*;

public class FileContentCopier {

    private static final int MAX_TOTAL_CHARS = 1_200_000;

    public static @NotNull CopyResult copyFilesMarkdown(@NotNull List<FileTreeNode> rootNodes) {
        StringBuilder out = new StringBuilder();

        List<FileEntry> files = new ArrayList<>();
        for (FileTreeNode root : rootNodes) {
            collectFiles(files, root, "");
        }
        files.sort(Comparator.comparing(a -> a.path.toLowerCase(Locale.ROOT)));

        int skippedBinary = 0;
        int skippedIgnored = 0;
        boolean truncated = false;
        int copiedFiles = 0;

        for (FileEntry fe : files) {
            VirtualFile vf = fe.file;

            if (IgnoreRules.shouldIgnore(vf)) {
                skippedIgnored++;
                continue;
            }
            if (IgnoreRules.isProbablyBinary(vf)) {
                skippedBinary++;
                continue;
            }

            if (out.length() > 0) out.append("\n\n");
            out.append("### ").append(fe.path).append("\n");

            String content;
            try {
                content = new String(vf.contentsToByteArray(), vf.getCharset());
            } catch (IOException e) {
                out.append("[Error reading file: ").append(e.getMessage()).append("]\n");
                continue;
            }

            // -------- Auto-safe fence: avoid ``` in content breaking Markdown --------
            String fence = makeSafeFence(content); // e.g. "```" or "````" or more
            String lang = guessFenceLanguage(vf);

            out.append(fence).append(lang).append("\n");

            int remaining = MAX_TOTAL_CHARS - out.length();
            if (remaining <= 0) {
                out.append("[TRUNCATED: exceeded MAX_TOTAL_CHARS]\n");
                out.append(fence);
                truncated = true;
                break;
            }

            // Reserve for: closing fence + newline + truncation message etc.
            int overhead = fence.length() + 2;
            if (content.length() + overhead > remaining) {
                int take = Math.max(0, remaining - 64);
                out.append(content, 0, Math.min(take, content.length()));
                if (!out.toString().endsWith("\n")) out.append("\n");
                out.append("[TRUNCATED: exceeded MAX_TOTAL_CHARS]\n");
                out.append(fence);
                truncated = true;
                break;
            }

            out.append(content);
            if (!content.endsWith("\n")) out.append("\n");
            out.append(fence);

            copiedFiles++;
        }

        String text = out.toString();
        CopyPasteManager.getInstance().setContents(new StringSelection(text));

        return new CopyResult(copiedFiles, skippedIgnored, skippedBinary, truncated, text.length());
    }

    public static void copyFilesWithHeaders(@NotNull List<FileTreeNode> rootNodes) {
        copyFilesMarkdown(rootNodes);
    }

    private static void collectFiles(List<FileEntry> out, FileTreeNode node, String relativePath) {
        String nodePath = relativePath.isEmpty()
                ? node.getFile().getName()
                : relativePath + "/" + node.getFile().getName();

        if (node.isDirectory()) {
            List<FileTreeNode> children = new ArrayList<>(node.getChildren());
            children.sort((a, b) -> {
                if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
                return a.getFile().getName().compareToIgnoreCase(b.getFile().getName());
            });

            for (FileTreeNode child : children) {
                collectFiles(out, child, nodePath);
            }
        } else {
            out.add(new FileEntry(node.getFile(), nodePath));
        }
    }

    /**
     * Returns a fence that won't conflict with content inside.
     * Rule: find the longest consecutive backtick run length maxTicks in content,
     * fence length = max(3, maxTicks + 1).
     */
    private static String makeSafeFence(String content) {
        int maxTicks = longestBacktickRun(content);
        int fenceLen = Math.max(3, maxTicks + 1);
        StringBuilder sb = new StringBuilder(fenceLen);
        for (int i = 0; i < fenceLen; i++) sb.append('`');
        return sb.toString();
    }

    private static int longestBacktickRun(String s) {
        int best = 0;
        int cur = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '`') {
                cur++;
                if (cur > best) best = cur;
            } else {
                cur = 0;
            }
        }
        return best;
    }

    private static String guessFenceLanguage(VirtualFile vf) {
        String ext = vf.getExtension();
        if (ext == null) return "";
        ext = ext.toLowerCase(Locale.ROOT);

        switch (ext) {
            case "java": return "java";
            case "kt":
            case "kts": return "kotlin";
            case "py": return "python";
            case "js": return "javascript";
            case "ts": return "typescript";
            case "jsx": return "jsx";
            case "tsx": return "tsx";
            case "go": return "go";
            case "rs": return "rust";
            case "c":
            case "h": return "c";
            case "cpp":
            case "hpp": return "cpp";
            case "cs": return "csharp";
            case "php": return "php";
            case "rb": return "ruby";
            case "swift": return "swift";
            case "scala": return "scala";
            case "sql": return "sql";
            case "xml": return "xml";
            case "yml":
            case "yaml": return "yaml";
            case "json": return "json";
            case "md": return "markdown";
            case "html": return "html";
            case "css": return "css";
            case "sh": return "bash";
            default: return "";
        }
    }

    private static final class FileEntry {
        final VirtualFile file;
        final String path;

        FileEntry(VirtualFile file, String path) {
            this.file = file;
            this.path = path;
        }
    }

    public static final class CopyResult {
        public final int copiedFiles;
        public final int skippedIgnored;
        public final int skippedBinary;
        public final boolean truncated;
        public final int totalChars;

        public CopyResult(int copiedFiles, int skippedIgnored, int skippedBinary, boolean truncated, int totalChars) {
            this.copiedFiles = copiedFiles;
            this.skippedIgnored = skippedIgnored;
            this.skippedBinary = skippedBinary;
            this.truncated = truncated;
            this.totalChars = totalChars;
        }
    }
}
