package org.intellij.sonar.configuration.check;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.sonar.sonarreport.SonarReport;
import org.intellij.sonar.util.MessagesUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.intellij.sonar.util.MessagesUtil.errorMessage;
import static org.intellij.sonar.util.MessagesUtil.okMessage;

public class SonarReportContentCheck implements Runnable, ConfigurationCheck {

  private final String pathToSonarReportJson;
  private SonarReport sonarReport;
  private String errorMessage;

  public SonarReportContentCheck(String pathToSonarReportJson) {
    this.pathToSonarReportJson = pathToSonarReportJson;
  }

  @Override
  public String getMessage() {
    if (isOk()) {
      return okMessage(String.format("version: %s, issues: %d, rules: %d, components: %d from %s\n"
          , sonarReport.getVersion()
          , sonarReport.getIssues().size()
          , sonarReport.getRules().size()
          , sonarReport.getComponents().size()
          , pathToSonarReportJson
      ));
    } else {
      return errorMessage(String.format("Cannot retrieve sonar report from %s\n\nRoot cause: %s\n", pathToSonarReportJson, errorMessage));
    }
  }

  @Override
  public boolean isOk() {
    return sonarReport != null && StringUtil.isEmptyOrSpaces(errorMessage);
  }

  @Override
  public void run() {
    try {
      String sonarReportContent = Files.toString(new File(pathToSonarReportJson), Charsets.UTF_8);
      sonarReport = SonarReport.fromJson(sonarReportContent);
    } catch (IOException e) {
      errorMessage = e.getMessage();
    }
  }
}
