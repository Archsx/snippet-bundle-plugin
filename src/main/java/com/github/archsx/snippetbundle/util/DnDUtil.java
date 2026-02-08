package com.github.archsx.snippetbundle.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

/**
 * Utility class for drag and drop operations.
 */
public class DnDUtil {

    /**
     * Finds the VirtualFile corresponding to the given File.
     * First tries to find it in the project, then falls back to LocalFileSystem.
     */
    public static VirtualFile findVirtualFile(Project project, File file) {
        // First try to find through LocalFileSystem
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        return virtualFile;
    }

    /**
     * Checks if a file is within the project base path.
     */
    public static boolean isInProject(Project project, VirtualFile file) {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) {
            return false;
        }
        return file.getUrl().startsWith(baseDir.getUrl());
    }
}
