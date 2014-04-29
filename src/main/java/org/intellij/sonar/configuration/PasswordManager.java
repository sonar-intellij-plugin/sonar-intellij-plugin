package org.intellij.sonar.configuration;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.UIUtil;

public final class PasswordManager {

  private static final Logger LOG = Logger.getInstance(PasswordManager.class);

  public static void storePassword(final String key, final String value) {
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        try {
          PasswordSafe.getInstance().storePassword(null, PasswordManager.class, key, value);
        } catch (PasswordSafeException e) {
          LOG.error("Cannot store password", e);
        }
      }
    });

  }

  private static String password;

  public static String loadPassword(final String key) {
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        try {
          password = PasswordSafe.getInstance().getPassword(null, PasswordManager.class, key);
        } catch (PasswordSafeException e) {
          LOG.error("Cannot get password", e);
        }
      }
    });
    return password;
  }
}
