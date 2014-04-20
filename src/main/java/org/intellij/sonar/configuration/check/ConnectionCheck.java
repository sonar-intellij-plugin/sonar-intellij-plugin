package org.intellij.sonar.configuration.check;

import com.google.common.base.Optional;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.intellij.sonar.sonarserver.SonarServer;

import static org.intellij.sonar.util.MessagesUtil.errorMessage;
import static org.intellij.sonar.util.MessagesUtil.okMessage;

public class ConnectionCheck implements Runnable, ConfigurationCheck {

  private final SonarServer mySonarServer;

  private Optional<String> mySonarServerVersion = Optional.absent();

  private Optional<String> mySonarServerError = Optional.absent();

  public ConnectionCheck(SonarServer sonarServer) {
    this.mySonarServer = sonarServer;
  }

  @Override
  public void run() {
    ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
    indicator.setText("Testing Connection");
    indicator.setText2(String.format("connecting to %s", mySonarServer.getSonarServerConfigurationBean().getHostUrl()));
    indicator.setFraction(0.5);
    indicator.setIndeterminate(true);

    try {
      mySonarServerVersion = Optional.fromNullable(mySonarServer.verifySonarConnection());
    } catch (Exception e) {
      mySonarServerError = Optional.of(e.getMessage());
      throw new ProcessCanceledException();
    }

    if (indicator.isCanceled()) {
      throw new ProcessCanceledException();
    }
  }

  @Override
  public boolean isOk() {
    return connectionWorks();
  }

  public boolean connectionWorks() {
    return !mySonarServerError.isPresent();
  }

  public Optional<String> getSonarServerVersion() {
    return mySonarServerVersion;
  }

  public Optional<String> getSonarServerError() {
    return mySonarServerError;
  }

  @Override
  public String getMessage() {

    if (!connectionWorks()) {
      return errorMessage(String.format("Connection to sonar server not successful\n\n%s\n", getSonarServerError().get()));
    } else {
      return okMessage(String.format("Sonar server version: %s\n", getSonarServerVersion().get()));
    }
  }
}
