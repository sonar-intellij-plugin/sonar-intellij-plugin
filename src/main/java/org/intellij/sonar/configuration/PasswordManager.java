package org.intellij.sonar.configuration;

import com.google.common.base.Throwables;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;

public final class PasswordManager {

  private static String password;

  public static void storePassword(final String key,final String value) {
    UIUtil.invokeAndWaitIfNeeded(
        (Runnable) () -> {
          try {
            PasswordSafe.getInstance().storePassword(null,PasswordManager.class,key,value);
          } catch (Exception e) {
            Messages.showErrorDialog("Cannot store password\n"+Throwables.getStackTraceAsString(e),"Error");
          }
        }
    );
  }

  public static String loadPassword(final String key) {
    UIUtil.invokeAndWaitIfNeeded(
        (Runnable) () -> {
          try {
            password = PasswordSafe.getInstance().getPassword(null,PasswordManager.class,key);
          } catch (Exception e) {
            Messages.showErrorDialog("Cannot load password\n"+Throwables.getStackTraceAsString(e),"Error");
          }
        }
    );
    return password;
  }
}
