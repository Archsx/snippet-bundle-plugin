package com.github.archsx.snippetbundle.ui;

import com.github.archsx.snippetbundle.model.FileTreeNode;
import com.github.archsx.snippetbundle.service.QuickCopyService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FileListPanel extends JPanel implements Scrollable {

    private List<FileTreeNode> rootNodes;
    private Runnable onTreeChanged;
    private QuickCopyService quickCopyService;

    public FileListPanel() {
        this.rootNodes = new ArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
    }

    public void setQuickCopyService(QuickCopyService service) {
        this.quickCopyService = service;
    }

    public List<FileTreeNode> getRootNodes() {
        return rootNodes;
    }

    public void setRootNodes(List<FileTreeNode> rootNodes) {
        this.rootNodes = rootNodes;
        rebuildTree();
    }

    public void setOnTreeChanged(Runnable callback) {
        this.onTreeChanged = callback;
    }

    public void rebuildTree() {
        removeAll();
        for (FileTreeNode rootNode : rootNodes) {
            addNodeToTree(rootNode, 0);
        }
        revalidate();
        repaint();

        if (onTreeChanged != null) onTreeChanged.run();
    }

    private void addNodeToTree(FileTreeNode node, int depth) {
        FileTreeItemPanel itemPanel = new FileTreeItemPanel(
                node,
                () -> {
                    if (quickCopyService != null) {
                        quickCopyService.removeNode(node);
                    }
                },
                this,
                depth
        );
        add(itemPanel);

        if (node.isDirectory() && node.isExpanded()) {
            for (FileTreeNode child : node.getChildren()) {
                addNodeToTree(child, depth + 1);
            }
        }
    }

    // -------- Scrollable --------
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
