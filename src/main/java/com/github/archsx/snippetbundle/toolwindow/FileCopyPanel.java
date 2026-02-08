package com.github.archsx.snippetbundle.toolwindow;

import com.github.archsx.snippetbundle.model.FileTreeNode;
import com.github.archsx.snippetbundle.service.FileContentCopier;
import com.github.archsx.snippetbundle.service.QuickCopyService;
import com.github.archsx.snippetbundle.ui.FileListPanel;
import com.github.archsx.snippetbundle.util.DnDUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileCopyPanel {

    private final Project project;
    private final JPanel mainPanel;
    private final FileListPanel fileListPanel;
    private JLabel dropZoneLabel;

    private JTextArea statusArea;

    private final QuickCopyService quickCopyService;

    public FileCopyPanel(Project project) {
        this.project = project;
        this.quickCopyService = project.getService(QuickCopyService.class);

        mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        fileListPanel = new FileListPanel();
        fileListPanel.setQuickCopyService(quickCopyService);
        fileListPanel.setOnTreeChanged(this::updateStatus);

        JScrollPane scrollPane = new JScrollPane(fileListPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Selected Files"));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel dropZone = createDropZone();
        mainPanel.add(dropZone, BorderLayout.NORTH);

        JPanel bottomPanel = createBottomPanelBigButtons();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setupDragAndDrop(dropZone);

        quickCopyService.addListener(() -> SwingUtilities.invokeLater(this::refreshFromService));
        refreshFromService();
    }

    private void refreshFromService() {
        fileListPanel.setRootNodes(quickCopyService.getRootNodesSnapshot());
        updateStatus();
        updateDropZoneLabel();
    }

    private JPanel createDropZone() {
        JPanel dropZone = new JPanel(new BorderLayout());
        dropZone.setPreferredSize(new Dimension(0, 80));
        dropZone.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        dropZone.setBackground(new Color(240, 240, 245));

        dropZoneLabel = new JLabel("Drag & Drop files or folders here", SwingConstants.CENTER);
        dropZoneLabel.setForeground(Color.DARK_GRAY);
        dropZoneLabel.setFont(dropZoneLabel.getFont().deriveFont(Font.PLAIN, 14f));
        dropZone.add(dropZoneLabel, BorderLayout.CENTER);

        return dropZone;
    }

    private JPanel createBottomPanelBigButtons() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 6));

        // --- Button row: Copy(主按钮占满) + Clear(固定宽度更小) ---
        JPanel buttonRow = new JPanel(new BorderLayout(10, 0));

        // 更短更直观：一眼看懂“把选中的文件复制成 Markdown”
        JButton copyButton = new JButton("Copy Files (Markdown)", AllIcons.Actions.Copy);
        copyButton.setToolTipText("Copy selected files as Markdown to clipboard");
        makePrimaryButton(copyButton);
        copyButton.addActionListener(e -> copyAllFiles());
        buttonRow.add(copyButton, BorderLayout.CENTER);

        JButton clearButton = new JButton("Clear", AllIcons.Actions.GC);
        clearButton.setToolTipText("Clear all selected files/folders");
        makeSecondaryButton(clearButton);
        clearButton.addActionListener(e -> clearAll());
        buttonRow.add(clearButton, BorderLayout.EAST);

        bottomPanel.add(buttonRow, BorderLayout.NORTH);

        // --- Status (2 lines, wrap) ---
        statusArea = new JTextArea(2, 10);
        statusArea.setEditable(false);
        statusArea.setOpaque(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setBorder(null);
        statusArea.setForeground(Color.GRAY);
        statusArea.setFont(UIManager.getFont("Label.font"));

        bottomPanel.add(statusArea, BorderLayout.SOUTH);

        return bottomPanel;
    }

    private void makePrimaryButton(JButton b) {
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setIconTextGap(8);
        b.setMargin(new Insets(10, 12, 10, 12));
        b.setPreferredSize(new Dimension(0, 42)); // 高一些，更好点
    }

    private void makeSecondaryButton(JButton b) {
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setIconTextGap(8);
        b.setMargin(new Insets(10, 12, 10, 12));

        // 固定一个更小的宽度（根据你字体大概够用）
        b.setPreferredSize(new Dimension(140, 42));
        b.setMinimumSize(new Dimension(140, 42));
    }

    private void clearAll() {
        if (fileListPanel.getRootNodes().isEmpty()) return;

        int ok = Messages.showYesNoDialog(
                project,
                "Clear all selected files/folders?",
                "Clear All",
                Messages.getQuestionIcon()
        );
        if (ok != Messages.YES) return;

        quickCopyService.clearAll();
    }

    private void setupDragAndDrop(JPanel dropZone) {
        dropZone.setDropTarget(new DropTarget(dropZone, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    dropZone.setBackground(new Color(200, 220, 255));
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override public void dragOver(DropTargetDragEvent dtde) {}
            @Override public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {
                dropZone.setBackground(new Color(240, 240, 245));
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                dropZone.setBackground(new Color(240, 240, 245));

                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = dtde.getTransferable();

                    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

                        List<VirtualFile> vfs = new ArrayList<>();
                        for (File f : fileList) {
                            VirtualFile vf = DnDUtil.findVirtualFile(project, f);
                            if (vf != null) vfs.add(vf);
                        }
                        quickCopyService.addFiles(vfs);

                        dtde.dropComplete(true);
                    } else {
                        dtde.dropComplete(false);
                    }
                } catch (Exception e) {
                    dtde.dropComplete(false);
                }
            }
        }));
    }

    private void updateStatus() {
        List<FileTreeNode> roots = fileListPanel.getRootNodes();
        int totalFiles = 0;
        for (FileTreeNode n : roots) totalFiles += n.getFileCount();

        statusArea.setText(String.format(
                "Total items: %d | Total files to copy: %d\n" +
                        "Tip: Project View right-click → Add to Quick Copy",
                roots.size(), totalFiles
        ));
    }

    private void updateDropZoneLabel() {
        if (fileListPanel.getRootNodes().isEmpty()) {
            dropZoneLabel.setText("Drag & Drop files or folders here");
        } else {
            dropZoneLabel.setText("Drag & Drop more files or folders here");
        }
    }

    private void copyAllFiles() {
        List<FileTreeNode> roots = fileListPanel.getRootNodes();
        if (roots.isEmpty()) {
            Messages.showInfoMessage(
                    "Please add files or folders first (Drag&Drop or Project View right-click).",
                    "No Files to Copy"
            );
            return;
        }

        try {
            FileContentCopier.CopyResult r = FileContentCopier.copyFilesMarkdown(roots);
            statusArea.setText(String.format(
                    "Copied %d files | chars: %d\n" +
                            "skipped ignored: %d | skipped binary: %d%s",
                    r.copiedFiles,
                    r.totalChars,
                    r.skippedIgnored,
                    r.skippedBinary,
                    r.truncated ? " | TRUNCATED" : ""
            ));
        } catch (Exception e) {
            Messages.showErrorDialog("Failed to copy files: " + e.getMessage(), "Copy Failed");
        }
    }

    public JComponent getContent() {
        return mainPanel;
    }
}
