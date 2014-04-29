package org.intellij.sonar.configuration.check;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.sonar.persistence.IncrementalScriptBean;
import org.intellij.sonar.sonarreport.data.SonarReport;

import java.io.File;
import java.util.List;

import static org.intellij.sonar.util.MessagesUtil.errorMessage;
import static org.intellij.sonar.util.MessagesUtil.okMessage;

public class SonarReportContentCheck implements Runnable, ConfigurationCheck {

  private final String pathToSonarReportJson;
  private SonarReport sonarReport;
  private String errorMessage;

  public SonarReportContentCheck(String pathToSonarReportJson) {
    this.pathToSonarReportJson = pathToSonarReportJson;
  }

  public static String checkSonarReportFiles(Project project, List<IncrementalScriptBean> incrementalScriptBeans) {
    StringBuilder sb = new StringBuilder();
    for (IncrementalScriptBean incrementalScriptBean : incrementalScriptBeans) {
      FileExistenceCheck fileExistenceCheck = new FileExistenceCheck(incrementalScriptBean.getPathToSonarReport());
      ProgressManager.getInstance().runProcessWithProgressSynchronously(
          fileExistenceCheck,
          "Testing File Existence", true, project
      );
      sb.append(fileExistenceCheck.getMessage());
      if (fileExistenceCheck.isOk()) {
        SonarReportContentCheck sonarReportContentCheck = new SonarReportContentCheck(incrementalScriptBean.getPathToSonarReport());
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            sonarReportContentCheck,
            "Testing Sonar Report Contents", true, project
        );
        sb.append(sonarReportContentCheck.getMessage());
      }
    }
    return sb.toString();
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
    } catch (Exception e) {
      errorMessage = e.getMessage();
    }
  }
}
