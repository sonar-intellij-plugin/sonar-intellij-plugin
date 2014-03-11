package org.intellij.sonar.configuration;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.intellij.sonar.configuration.project.ProjectSettingsBean;

public final class PasswordManager {

  private static final Logger LOG = Logger.getInstance(PasswordManager.class);

  private static String getPasswordSafeKey(Project project, String user) {
    return String.format("%s-%s", project.getName(), user);
  }

  public static void storePassword(final Project project, ProjectSettingsBean projectSettingsBean) {
    String user = projectSettingsBean.user;
    final String passwordValue = projectSettingsBean.password;
    final String passwordSafeKey = getPasswordSafeKey(project, user);

    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        try {
          PasswordSafe.getInstance().storePassword(project, PasswordManager.class, passwordSafeKey, passwordValue);
        } catch (PasswordSafeException e) {
          LOG.warn("Cannot store password", e);
        }
      }
    });

  }

  private static String password;

  public static String loadPassword(final Project project, ProjectSettingsBean projectSettingsBean) {
    final String user = projectSettingsBean.user;
    final String passwordSafeKey = getPasswordSafeKey(project, user);

    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        try {
          password = PasswordSafe.getInstance().getPassword(project, PasswordManager.class, passwordSafeKey);
        } catch (PasswordSafeException e) {
          LOG.warn("Cannot get password", e);
        }
      }
    });
    return password;


  }
}
