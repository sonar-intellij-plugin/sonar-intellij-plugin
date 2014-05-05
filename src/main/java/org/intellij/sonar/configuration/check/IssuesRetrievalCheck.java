package org.intellij.sonar.configuration.check;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.sonar.sonarserver.SonarServer;
import org.sonar.wsclient.issue.Issues;
import org.sonar.wsclient.services.Resource;

import java.util.List;

import static org.intellij.sonar.util.MessagesUtil.*;

public class IssuesRetrievalCheck implements Runnable, ConfigurationCheck {

  private final SonarServer mySonarServer;
  private final String myResourceKey;

  private Issues myIssues;
  private Resource myResource;

  private String errorMessage;

  public IssuesRetrievalCheck(SonarServer sonarServer, String resourceKey) {
    this.mySonarServer = sonarServer;
    this.myResourceKey = resourceKey;
  }

  public static String checkIssuesRetrieval(SonarServer sonarServer, Project project, List<Resource> resources) {
    if (null == resources || resources.size() == 0) return "";

    StringBuilder sb = new StringBuilder();
    for (Resource resource : resources) {
      final String resourceKey = resource.getKey();
      final IssuesRetrievalCheck issuesRetrievalCheck = new IssuesRetrievalCheck(sonarServer, resourceKey);
      ProgressManager.getInstance().runProcessWithProgressSynchronously(
          issuesRetrievalCheck,
          "Testing Issues", true, project
      );
      sb.append(issuesRetrievalCheck.getMessage());
    }

    return sb.toString();
  }

  @Override
  public void run() {
    try {
      myIssues = mySonarServer.getIssuesFor(myResourceKey);
      myResource = mySonarServer.getResourceWithProfile(myResourceKey);
    } catch (Exception e) {
      errorMessage = e.getMessage();
    }
  }

  @Override
  public boolean isOk() {
    return StringUtil.isEmptyOrSpaces(errorMessage)
        && !(null == myIssues || null == myIssues.maxResultsReached())
        && !myIssues.maxResultsReached();
  }

  @Override
  public String getMessage() {
    if (!StringUtil.isEmptyOrSpaces(errorMessage)) {
      return errorMessage(String.format("Cannot retrieve issues for %s\nRoot cause:\n\n%s\n", myResourceKey, errorMessage));
    }
    if (null == myIssues || null == myIssues.maxResultsReached()) {
      return errorMessage(String.format("Cannot retrieve issues for %s\n", myResourceKey));
    } else if (null == myResource) {
      return errorMessage(String.format("Cannot retrieve resource for %s\n", myResourceKey));
    } else if (null == myIssues.paging()) {
      return errorMessage("Cannot retrieve issues count. Empty Paging.\n");
    } else if (myIssues.maxResultsReached()) {
      return warnMessage(String.format("Max results reached for %s !" +
          " Total issues size is greater then %s.\n", myResource.getName(), myIssues.paging().total()));
    } else {
      return okMessage(String.format("Total issues count for %s: %d.\n", myResource.getName(), myIssues.paging().total()));
    }
  }
}
