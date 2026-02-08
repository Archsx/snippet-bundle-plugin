package com.github.archsx.snippetbundle.service;

import com.github.archsx.snippetbundle.model.FileTreeNode;
import com.github.archsx.snippetbundle.util.IgnoreRules;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service(Service.Level.PROJECT)
public final class QuickCopyService {

    private final List<FileTreeNode> rootNodes = new ArrayList<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public void addListener(@NotNull Runnable r) {
        listeners.add(r);
    }

    public void removeListener(@NotNull Runnable r) {
        listeners.remove(r);
    }

    public @NotNull List<FileTreeNode> getRootNodesSnapshot() {
        return new ArrayList<>(rootNodes);
    }

    public void addFiles(@NotNull List<VirtualFile> files) {
        int added = 0;

        for (VirtualFile vf : files) {
            if (vf == null || !vf.isValid()) continue;
            if (IgnoreRules.shouldIgnore(vf)) continue;

            boolean exists = false;
            for (FileTreeNode n : rootNodes) {
                if (n.getFile().equals(vf)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                rootNodes.add(new FileTreeNode(vf));
                added++;
            }
        }

        if (added > 0) notifyChanged();
    }

    public void removeNode(@NotNull FileTreeNode nodeToRemove) {
        rootNodes.removeIf(n -> n == nodeToRemove);
        for (FileTreeNode root : rootNodes) {
            removeNodeRecursive(root, nodeToRemove);
        }
        notifyChanged();
    }

    private boolean removeNodeRecursive(FileTreeNode parent, FileTreeNode toRemove) {
        if (parent.getChildren().remove(toRemove)) return true;
        for (FileTreeNode child : parent.getChildren()) {
            if (removeNodeRecursive(child, toRemove)) return true;
        }
        return false;
    }

    private void notifyChanged() {
        for (Runnable r : listeners) r.run();
    }

    public void clearAll() {
        if (rootNodes.isEmpty()) return;
        rootNodes.clear();
        notifyChanged();
    }

}
