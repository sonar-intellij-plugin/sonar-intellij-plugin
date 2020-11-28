package org.intellij.sonar.configuration;

import com.google.common.base.Throwables;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;

public final class PasswordManager {

  private static String password;

  public static void storePassword(final String user,final String password) {
    UIUtil.invokeAndWaitIfNeeded(
        (Runnable) () -> {
          try {
            PasswordSafe.getInstance()
                    .set(new CredentialAttributes(PasswordManager.class.getName(), user)
                    , new Credentials(user, password));
          } catch (Exception e) {
            Messages.showErrorDialog("Cannot store password\n"+Throwables.getStackTraceAsString(e),"Error");
          }
        }
    );
  }

  public static String loadPassword(final String user) {
    UIUtil.invokeAndWaitIfNeeded(
        (Runnable) () -> {
          try {
            password = PasswordSafe.getInstance()
                    .get(new CredentialAttributes(PasswordManager.class.getName(), user))
                    .getPasswordAsString();
          } catch (Exception e) {
            Messages.showErrorDialog("Cannot load password\n"+Throwables.getStackTraceAsString(e),"Error");
          }
        }
    );
    return password;
  }
}
