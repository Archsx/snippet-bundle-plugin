package com.github.archsx.snippetbundle.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.InplaceButton;
import com.intellij.util.ui.JBUI;
import com.github.archsx.snippetbundle.model.FileTreeNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FileTreeItemPanel extends JPanel {

    private final FileTreeNode node;
    private final Runnable onRemove;
    private final JButton expandButton;
    private final InplaceButton removeButton;
    private boolean hovered = false;
    private static Integer rowHeight = null;

    public FileTreeItemPanel(FileTreeNode node, Runnable onRemove, JPanel childrenContainer, int depth) {
        this.node = node;
        this.onRemove = onRemove;

        if (rowHeight == null) rowHeight = calculateRowHeightStatic();

        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // IMPORTANT: preferred width 不能用 Integer.MAX_VALUE，否则 JScrollPane 会裁剪到看不见右侧
        setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        setPreferredSize(new Dimension(0, rowHeight));
        setMinimumSize(new Dimension(0, rowHeight));

        // ---------------- leftPanel: BoxLayout.X_AXIS ----------------
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);

        // Indent
        if (depth > 0) {
            leftPanel.add(Box.createRigidArea(new Dimension(depth * JBUI.scale(20), 0)));
        }

        // Expand / spacer
        if (node.isDirectory()) {
            expandButton = createExpandButton();
            leftPanel.add(expandButton);
        } else {
            leftPanel.add(Box.createRigidArea(new Dimension(JBUI.scale(16), 0)));
            expandButton = null;
        }

        leftPanel.add(Box.createRigidArea(new Dimension(JBUI.scale(4), 0)));

        // Icon
        JLabel iconLabel = new JLabel(getIconForNode(node));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, JBUI.scale(6)));
        leftPanel.add(iconLabel);

        // Name
        JLabel nameLabel = new JLabel(node.getFile().getName());
        if (node.isDirectory()) nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        leftPanel.add(nameLabel);

        // Gap after name: ~3-5 个中文字宽（默认 4）
        leftPanel.add(Box.createRigidArea(new Dimension(calcGapAfterNamePx(leftPanel, 4), 0)));

        // Remove button
        removeButton = new InplaceButton("Remove", AllIcons.General.Remove, e -> {
            if (this.onRemove != null) this.onRemove.run();
        });
        removeButton.setVisible(false);
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeButton.setBorder(JBUI.Borders.empty(2, 6));
        leftPanel.add(removeButton);

        // 关键：吃掉剩余空间，保证按钮跟在文件名后
        leftPanel.add(Box.createHorizontalGlue());

        add(leftPanel, BorderLayout.CENTER);

        // ---------------- hover logic (整行高亮 + 按钮显示) ----------------
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                removeButton.setVisible(true);
                revalidate();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 防抖：只有当鼠标真正离开整个 row 才隐藏
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), FileTreeItemPanel.this);
                if (!FileTreeItemPanel.this.contains(p)) {
                    hovered = false;
                    removeButton.setVisible(false);
                    revalidate();
                    repaint();
                }
            }
        };

        this.addMouseListener(hoverAdapter);
        leftPanel.addMouseListener(hoverAdapter);
        removeButton.addMouseListener(hoverAdapter);

        // Tooltip = full path
        VirtualFile vf = node.getFile();
        if (vf != null) setToolTipText(vf.getPresentableUrl());
    }

    @Override
    protected void paintComponent(Graphics g) {
        // hover 时整行轻微高亮
        if (hovered) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                Color base = UIManager.getColor("List.selectionBackground");
                if (base == null) base = new Color(0, 0, 0);
                g2.setComposite(AlphaComposite.SrcOver.derive(0.12f));
                g2.setColor(base);
                int arc = JBUI.scale(8);
                g2.fillRoundRect(JBUI.scale(2), JBUI.scale(2),
                        getWidth() - JBUI.scale(4),
                        getHeight() - JBUI.scale(4),
                        arc, arc);
            } finally {
                g2.dispose();
            }
        }
        super.paintComponent(g);
    }

    private static int calculateRowHeightStatic() {
        JLabel label = new JLabel();
        FontMetrics fm = label.getFontMetrics(label.getFont());
        int fontHeight = fm.getHeight();
        int h = (int) (fontHeight * 1.75f);
        return Math.max(JBUI.scale(28), h);
    }

    private static int calcGapAfterNamePx(JComponent c, int chars) {
        FontMetrics fm = c.getFontMetrics(c.getFont());
        int w = fm.charWidth('中');
        if (w <= 0) w = fm.charWidth('W');
        return JBUI.scale(Math.max(8, w * chars));
    }

    private Icon getIconForNode(FileTreeNode node) {
        return node.isDirectory()
                ? UIManager.getIcon("Tree.folderIcon")
                : UIManager.getIcon("Tree.fileIcon");
    }

    private JButton createExpandButton() {
        JButton button = new JButton(node.isExpanded() ? "▼" : "▶");
        int buttonSize = Math.max(JBUI.scale(16), (int) (rowHeight * 0.7));
        Dimension d = new Dimension(buttonSize, buttonSize);
        button.setPreferredSize(d);
        button.setMinimumSize(d);
        button.setMaximumSize(d);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusable(false);
        button.setOpaque(false);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 9f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> toggleExpand());
        return button;
    }

    private void toggleExpand() {
        node.toggleExpanded();
        if (expandButton != null) expandButton.setText(node.isExpanded() ? "▼" : "▶");
        rebuildParentView();
    }

    private void rebuildParentView() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof FileListPanel)) parent = parent.getParent();
        if (parent instanceof FileListPanel) ((FileListPanel) parent).rebuildTree();
    }
}
