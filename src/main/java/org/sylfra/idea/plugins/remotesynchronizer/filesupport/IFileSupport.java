package org.sylfra.idea.plugins.remotesynchronizer.filesupport;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 */
public interface IFileSupport
{
  VirtualFile[] getSelectedFiles(DataContext dataContext);

  boolean insideModule(DataContext dataContext);

  String getFileCompilerPaths(VirtualFile f);
}
