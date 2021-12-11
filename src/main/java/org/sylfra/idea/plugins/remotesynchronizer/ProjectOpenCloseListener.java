package org.sylfra.idea.plugins.remotesynchronizer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.remotesynchronizer.utils.Utils;

public class ProjectOpenCloseListener implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {
        project.getService(RemoteSynchronizerPlugin.class).projectOpened();
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        project.getService(RemoteSynchronizerPlugin.class).projectClosed();
    }
}
