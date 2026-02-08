package com.github.archsx.snippetbundle.model;

import com.github.archsx.snippetbundle.util.IgnoreRules;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the file tree.
 * Can be either a file (leaf) or a directory (with children).
 */
public class FileTreeNode {

    private final VirtualFile file;
    private final FileTreeNode parent;
    private final List<FileTreeNode> children;
    private boolean expanded;

    public FileTreeNode(VirtualFile file) {
        this(file, null);
    }

    public FileTreeNode(VirtualFile file, FileTreeNode parent) {
        this.file = file;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.expanded = false;

        // If directory, load children
        if (file.isDirectory()) {
            loadChildren();
        }
    }

    private void loadChildren() {
        VirtualFile[] childFiles = file.getChildren();
        if (childFiles != null) {
            for (VirtualFile child : childFiles) {
                // Default ignore rules: .git/node_modules/build/... + large files/binary files
                if (IgnoreRules.shouldIgnore(child)) {
                    continue;
                }
                children.add(new FileTreeNode(child, this));
            }
        }
    }

    public VirtualFile getFile() {
        return file;
    }

    public FileTreeNode getParent() {
        return parent;
    }

    public List<FileTreeNode> getChildren() {
        return children;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }

    /**
     * Gets the display path (relative to root or full path)
     */
    public String getDisplayPath() {
        if (parent == null) {
            return file.getName();
        }
        return parent.getDisplayPath() + "/" + file.getName();
    }

    /**
     * Counts total files in this node (recursively)
     */
    public int getFileCount() {
        if (!isDirectory()) {
            return 1;
        }
        int count = 0;
        for (FileTreeNode child : children) {
            count += child.getFileCount();
        }
        return count;
    }

    /**
     * Finds a node by file and removes it
     */
    public boolean removeChild(VirtualFile fileToRemove) {
        for (int i = 0; i < children.size(); i++) {
            FileTreeNode child = children.get(i);
            if (child.getFile().equals(fileToRemove)) {
                children.remove(i);
                return true;
            }
            if (child.isDirectory() && child.removeChild(fileToRemove)) {
                return true;
            }
        }
        return false;
    }
}
