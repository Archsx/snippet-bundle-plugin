package com.github.archsx.snippetbundle.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for creating the Quick File Copy tool window.
 */
public class MyToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        FileCopyPanel panel = new FileCopyPanel(project);
        Content content = contentFactory.createContent(panel.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
