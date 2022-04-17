package org.sylfra.idea.plugins.remotesynchronizer;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jdom.Element;
import org.sylfra.idea.plugins.remotesynchronizer.javasupport.IJavaSupport;
import org.sylfra.idea.plugins.remotesynchronizer.javasupport.NoJavaSupport;
import org.sylfra.idea.plugins.remotesynchronizer.model.Config;
import org.sylfra.idea.plugins.remotesynchronizer.synchronizing.SynchronizerThreadManager;
import org.sylfra.idea.plugins.remotesynchronizer.ui.ThreadConsolePane;
import org.sylfra.idea.plugins.remotesynchronizer.ui.ToolPanel;
import org.sylfra.idea.plugins.remotesynchronizer.ui.config.ConfigPanel;
import org.sylfra.idea.plugins.remotesynchronizer.utils.ConfigExternalizer;
import org.sylfra.idea.plugins.remotesynchronizer.utils.ConfigPathsManager;
import org.sylfra.idea.plugins.remotesynchronizer.utils.ConfigStateComponent;
import org.sylfra.idea.plugins.remotesynchronizer.utils._OldConfigExternalizer;

import javax.swing.*;
import java.net.URL;

/**
 * Plugin main class
 */
public class RemoteSynchronizerPlugin implements Configurable
{
  public static final String PLUGIN_NAME = "RemoteSynchronizer";

  public static final String WINDOW_ACTIONS_NAME = PLUGIN_NAME + "Window";
  public static final String ACTION_CLEAR_CONSOLE_NAME = "ConsoleClearAction";
  public static final String ACTION_REMOVE_CONSOLE_NAME = "ConsoleRemoveAction";
  public static final String ACTION_INTERRUPT_THREAD_NAME = "ThreadInterruptAction";
  public static final String ACTION_STOP_THREAD_NAME = "ThreadStopAction";
  public static final String ACTION_RERUN_LAST_SYNCHRO_NAME = "RerunLastSynchroAction";

  // Current project
  private Project project;
  // Provides support for paths management
  private ConfigPathsManager pathManager;
  // Manage threaded copies
  private SynchronizerThreadManager copierThreadManager;
  // Contains different consoles
  private ThreadConsolePane consolePane;
  // Settings panel
  private ConfigPanel panel;
  private IJavaSupport javaSupport;

  public RemoteSynchronizerPlugin(Project project)
  {
    this.project = project;

    pathManager = new ConfigPathsManager(this);
    javaSupport = project.getService(IJavaSupport.class);
    if (javaSupport == null)
    {
      javaSupport = new NoJavaSupport();
    }

    consolePane = new ThreadConsolePane(this);
    copierThreadManager = new SynchronizerThreadManager(this);
  }

  public Project getProject()
  {
    return project;
  }

  public Config getConfig()
  {
    return getStateComponent().getState();
  }

  public SynchronizerThreadManager getCopierThreadManager()
  {
    return copierThreadManager;
  }

  public ConfigPathsManager getPathManager()
  {
    return pathManager;
  }

  public ThreadConsolePane getConsolePane()
  {
    return consolePane;
  }

  public String getDisplayName()
  {
    return PLUGIN_NAME;
  }

  public Icon getIcon()
  {
    return new ImageIcon(getResource("logo-big.png"));
  }

  public String getHelpTopic()
  {
    return null;
  }

  public JComponent createComponent()
  {
    panel = new ConfigPanel(this);

    return panel;
  }

  public boolean isModified()
  {
    return panel.isModified(getConfig());
  }

  public void apply()
    throws ConfigurationException
  {
    Config config = getConfig();

    panel.apply(config);
    config.fireConfigChanged();
  }

  public void reset()
  {
    panel.reset(getConfig());
  }

  public void disposeUIResources()
  {
    panel = null;
  }

  public void projectOpened()
  {
    initToolWindow();
  }

  public void projectClosed()
  {
  }

  private void initToolWindow()
  {
    ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
    toolWindowManager.invokeLater(() -> {
      ToolWindow toolwindow = toolWindowManager.registerToolWindow(PLUGIN_NAME, true, ToolWindowAnchor.BOTTOM);
      ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
      Content content = contentFactory.createContent(new ToolPanel(consolePane, getConfig()),
              PLUGIN_NAME, true);
      toolwindow.getContentManager().addContent(content);

      toolwindow.setIcon(new ImageIcon(getResource("logo-small.png")));
    });
  }

  public static RemoteSynchronizerPlugin getInstance(Project project)
  {
    return project.getService(RemoteSynchronizerPlugin.class);
  }

  public ConfigExternalizer getConfigExternalizer()
  {
    return project.getService(ConfigExternalizer.class);
  }

  public IJavaSupport getJavaSupport()
  {
    return javaSupport;
  }

  public static URL getResource(String relativePath)
  {
    return RemoteSynchronizerPlugin.class.getResource("resources/" + relativePath);
  }

  /**
   * Provides settings component
   *
   * @return settings component
   */
  public ConfigStateComponent getStateComponent()
  {
    return project.getService(ConfigStateComponent.class);
  }

  public void launchSyncIfAllowed(VirtualFile[] files)
  {
    // Check if configuration allows concurrent runs when a synchro is running
    if ((!getStateComponent().getState().getGeneralOptions().isAllowConcurrentRuns())
      && (copierThreadManager.hasRunningSynchro()))
    {
      consolePane.doPopup();
      return;
    }

    copierThreadManager.launchSynchronization(files);
  }
}
