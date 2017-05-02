package org.sylfra.idea.plugins.remotesynchronizer.filesupport;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.remotesynchronizer.utils.PathsUtils;

/**
 *
 */
public class DefaultFileSupport extends AbstractProjectComponent implements IFileSupport {

    private final Project project;

    protected DefaultFileSupport(Project project)
    {
        super(project);
        this.project = project;
    }

    @Override
    public VirtualFile[] getSelectedFiles(DataContext dataContext) {
        VirtualFile[] selectedFiles = DataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        if (selectedFiles == null)
        {
            Module module = DataKeys.MODULE.getData(dataContext);
            return (module == null) ? null : ModuleRootManager.getInstance(module).getContentRoots();
        }
        return selectedFiles;
    }

    @Override
    public boolean insideModule(DataContext dataContext) {
        return (DataKeys.MODULE.getData(dataContext) != null);
    }

    @Override
    public String getFileCompilerPaths(VirtualFile f) {
        String outputPath = getOutputPath(f);
        if (outputPath == null)
        {
            return null;
        }
        return outputPath+"/"+getRelativeOutputPath(f.getCanonicalPath());
    }

    public String getOutputPath(VirtualFile f)
    {
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project)
                .getFileIndex();
        Module module = fileIndex.getModuleForFile(f);

        VirtualFile vFile;
        if (fileIndex.isInTestSourceContent(f))
        {
            vFile = CompilerModuleExtension.getInstance(module).getCompilerOutputPathForTests();
        }
        else if (fileIndex.isInSourceContent(f))
        {
            vFile = CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
        }
        else
        {
            vFile = null;
        }

        if (vFile == null)
        {
            return null;
        }

        return PathsUtils.toModelPath(vFile.getPresentableUrl());
    }

    public String getRelativeOutputPath(String path)
    {
        VirtualFile f = PathsUtils.getVirtualFile(path);
        if (f == null)
            return path;

        return getRelativePath(f);
    }

    public String getRelativePath(VirtualFile f)
    {
        return PathsUtils.getRelativePath(
                ProjectRootManager.getInstance(project).getContentRoots(), f);
    }
}
