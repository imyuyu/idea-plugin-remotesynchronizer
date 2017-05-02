package org.sylfra.idea.plugins.remotesynchronizer.filesupport;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.remotesynchronizer.javasupport.IJavaSupport;

import java.util.Collections;
import java.util.List;

/**
 */
public class NoFileSupport implements IFileSupport
{
  public VirtualFile[] getSelectedFiles(DataContext dataContext)
  {
    return CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
  }

  public boolean insideModule(DataContext dataContext)
  {
    return false;
  }

  public String getFileCompilerPaths(VirtualFile f) {
    return null;
  }


}
