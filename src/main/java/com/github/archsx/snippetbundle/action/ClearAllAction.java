package com.github.archsx.snippetbundle.action;

import com.github.archsx.snippetbundle.service.QuickCopyService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ClearAllAction extends AnAction {

    public ClearAllAction() {
        super("Clear All", "Clear all selected files/folders", AllIcons.Actions.GC);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        QuickCopyService svc = project.getService(QuickCopyService.class);
        if (svc.getRootNodesSnapshot().isEmpty()) return;

        int ok = Messages.showYesNoDialog(
                project,
                "Clear all selected files/folders?",
                "Clear All",
                Messages.getQuestionIcon()
        );
        if (ok != Messages.YES) return;

        svc.clearAll();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean enabled = false;
        if (project != null) {
            QuickCopyService svc = project.getService(QuickCopyService.class);
            enabled = !svc.getRootNodesSnapshot().isEmpty();
        }
        e.getPresentation().setEnabled(enabled);
    }
}
