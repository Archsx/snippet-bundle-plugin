package com.github.archsx.snippetbundle.action;

import com.github.archsx.snippetbundle.service.QuickCopyService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AddToSnippetBundleAction extends AnAction {

    public AddToSnippetBundleAction() {
        super("Add to SnippetBundle");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        List<VirtualFile> files = collectSelectedFiles(e);
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(!files.isEmpty());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        List<VirtualFile> files = collectSelectedFiles(e);
        if (files.isEmpty()) return;

        QuickCopyService svc = project.getService(QuickCopyService.class);
        if (svc == null) return;

        svc.addFiles(files);
    }

    @NotNull
    private static List<VirtualFile> collectSelectedFiles(@NotNull AnActionEvent e) {
        LinkedHashSet<VirtualFile> out = new LinkedHashSet<>();

        // 1) Classic keys (may be missing in 2025 Project View V2)
        VirtualFile[] vfa = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (vfa != null) Collections.addAll(out, vfa);

        VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (vf != null) out.add(vf);

        // 2) PSI array (very common in newer views)
        PsiElement[] psiArr = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiArr != null) {
            for (PsiElement el : psiArr) {
                VirtualFile f = toVirtualFile(el);
                if (f != null) out.add(f);
            }
        }

        // 3) Single PSI element / file
        PsiElement psi = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (psi != null) {
            VirtualFile f = toVirtualFile(psi);
            if (f != null) out.add(f);
        }

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null && psiFile.getVirtualFile() != null) {
            out.add(psiFile.getVirtualFile());
        }

        // 4) Navigatable keys (often the ONLY thing available in some 2025 contexts)
        Navigatable nav = e.getData(CommonDataKeys.NAVIGATABLE);
        VirtualFile navFile = toVirtualFile(nav);
        if (navFile != null) out.add(navFile);

        Navigatable[] navArr = e.getData(CommonDataKeys.NAVIGATABLE_ARRAY);
        if (navArr != null) {
            for (Navigatable n : navArr) {
                VirtualFile f = toVirtualFile(n);
                if (f != null) out.add(f);
            }
        }

        return new ArrayList<>(out);
    }

    private static VirtualFile toVirtualFile(PsiElement el) {
        if (el instanceof PsiFile) {
            return ((PsiFile) el).getVirtualFile();
        }
        if (el instanceof PsiDirectory) {
            return ((PsiDirectory) el).getVirtualFile();
        }
        PsiFile containing = el.getContainingFile();
        return containing != null ? containing.getVirtualFile() : null;
    }

    private static VirtualFile toVirtualFile(Navigatable nav) {
        if (nav == null) return null;
        if (nav instanceof PsiElement) {
            return toVirtualFile((PsiElement) nav);
        }
        if (nav instanceof OpenFileDescriptor) {
            return ((OpenFileDescriptor) nav).getFile();
        }
        return null;
    }
}
