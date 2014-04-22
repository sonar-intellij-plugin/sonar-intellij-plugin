package org.intellij.sonar.configuration.check;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.sonar.sonarserver.SonarServer;
import org.sonar.wsclient.services.Profile;
import org.sonar.wsclient.services.Resource;

import java.util.List;

import static org.intellij.sonar.util.MessagesUtil.errorMessage;
import static org.intellij.sonar.util.MessagesUtil.okMessage;

public class RulesRetrievalCheck implements Runnable, ConfigurationCheck {

  private final SonarServer mySonarServer;

  private final String myResourceKey;

  private Resource myResourceWithProfile;

  private Profile myProfile;

  private String myProfileName;

  private String myLanguage;

  private String errorMessage;

  public RulesRetrievalCheck(SonarServer mySonarServer, String resourceKey) {
    this.mySonarServer = mySonarServer;
    this.myResourceKey = resourceKey;
  }

  public static String checkRulesRetrieval(SonarServer sonarServer, Project project, List<Resource> resources) {

    if (null == resources || resources.size() == 0)
      return errorMessage("No sonar resource configured");

    StringBuilder sb = new StringBuilder();
    for (Resource resource : resources) {
      final String resourceKey = resource.getKey();
      final RulesRetrievalCheck rulesRetrievalCheck = new RulesRetrievalCheck(sonarServer, resourceKey);
      ProgressManager.getInstance().runProcessWithProgressSynchronously(
          rulesRetrievalCheck,
          "Testing Rules", true, project
      );
      sb.append(rulesRetrievalCheck.getMessage());
    }

    return sb.toString();
  }

  @Override
  public void run() {
    try {
      myResourceWithProfile = mySonarServer.getResourceWithProfile(myResourceKey);
      myLanguage = myResourceWithProfile.getLanguage();
      myProfileName = myResourceWithProfile.getMeasure("profile").getData();
      myProfile = mySonarServer.getProfile(myLanguage, myProfileName);
    } catch (Exception e) {
      errorMessage = e.getMessage();
    }
  }


  @Override
  public boolean isOk() {
    return StringUtil.isEmptyOrSpaces(errorMessage)
        && myResourceWithProfile != null && myProfile != null && myProfile.getRules() != null;
  }

  @Override
  public String getMessage() {
    if (!StringUtil.isEmptyOrSpaces(errorMessage)) {
      return errorMessage(createGeneralErrorMessage());
    }
    try {
      return okMessage(String.format("%s : %d rules from %s, %s\n",
              myResourceWithProfile.getName(), myProfile.getRules().size(), myProfileName, myLanguage)
          );
    } catch (Exception e) {
      errorMessage = e.getMessage();
      return errorMessage(createGeneralErrorMessage());
    }
  }

  private String createGeneralErrorMessage() {
    return String.format("Cannot retrieve rules for %s\nRoot Cause:\n\n%s\n", myResourceKey, errorMessage);
  }
}
