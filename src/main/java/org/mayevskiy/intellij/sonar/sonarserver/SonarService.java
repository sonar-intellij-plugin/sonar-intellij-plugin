package org.mayevskiy.intellij.sonar.sonarserver;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mayevskiy.intellij.sonar.SonarRulesProvider;
import org.mayevskiy.intellij.sonar.SonarSettingsBean;
import org.mayevskiy.intellij.sonar.SonarSeverity;
import org.mayevskiy.intellij.sonar.SonarViolationsProvider;
import org.mayevskiy.intellij.sonar.util.GuaveStreamUtil;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;
import org.sonar.wsclient.services.Rule;
import org.sonar.wsclient.services.RuleQuery;
import org.sonar.wsclient.services.Violation;
import org.sonar.wsclient.services.ViolationQuery;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Mayevskiy
 * @author Michail Plushnikov
 */
public class SonarService {

  private static final String VERSION_URL = "api/server/version";
  private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 3000;
  private static final int READ_TIMEOUT_IN_MILLISECONDS = 6000;
  private static final String USER_AGENT = "Sonar IntelliJ Connector";

  public SonarService() {
  }

  public String verifySonarConnection(SonarSettingsBean sonarSettingsBean) throws SonarServerConnectionException {
    HttpURLConnection httpURLConnection = getHttpConnection(sonarSettingsBean.host);

    try {
      int statusCode = httpURLConnection.getResponseCode();
      if (statusCode != HttpURLConnection.HTTP_OK) {
        throw new SonarServerConnectionException("ResponseCode: %d Url: %s", statusCode, httpURLConnection.getURL());
      }
      return GuaveStreamUtil.toString(httpURLConnection.getInputStream());
    } catch (IOException e) {
      throw new SonarServerConnectionException("Couldn't read data from url: %s", e, httpURLConnection.getURL());
    }
  }

  private HttpURLConnection getHttpConnection(String hostName) throws SonarServerConnectionException {
    URL sonarServerUrl = null;
    try {
      sonarServerUrl = new URL((hostName.endsWith("/") ? hostName : (hostName + "/")) + VERSION_URL);

      HttpURLConnection connection = (HttpURLConnection) sonarServerUrl.openConnection();
      connection.setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS);
      connection.setReadTimeout(READ_TIMEOUT_IN_MILLISECONDS);
      connection.setInstanceFollowRedirects(true);
      connection.setRequestProperty("User-Agent", USER_AGENT);
      return connection;
    } catch (MalformedURLException e) {
      throw new SonarServerConnectionException("Invalid url: %s", e, hostName);
    } catch (IOException e) {
      throw new SonarServerConnectionException("Couldn't connect to url: %s", e, sonarServerUrl.toString());
    }
  }

  @Nullable
  public List<Violation> getViolations(SonarSettingsBean sonarSettingsBean) {
    if (null == sonarSettingsBean) {
      return null;
    }
    Sonar sonar = Sonar.create(sonarSettingsBean.host, sonarSettingsBean.user, sonarSettingsBean.password);
    ViolationQuery violationQuery = ViolationQuery.createForResource(sonarSettingsBean.resource);
    violationQuery.setDepth(-1);
    violationQuery.setSeverities("BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO");

    return sonar.findAll(violationQuery);
  }


  public Collection<Rule> getAllRules(Collection<SonarSettingsBean> sonarSettingsBeans) {
    List<Rule> rulesResult = new LinkedList<Rule>();
    Set<String> ruleKeys = new LinkedHashSet<String>();
    if (null != sonarSettingsBeans) {
      for (SonarSettingsBean sonarSettingsBean : sonarSettingsBeans) {
        // for all SettingsBeans do:
        // find language
        String resourceUrl = sonarSettingsBean.resource;
        if (StringUtils.isNotBlank(resourceUrl)) {
          Sonar sonar = Sonar.create(sonarSettingsBean.host, sonarSettingsBean.user, sonarSettingsBean.password);
          ResourceQuery query = ResourceQuery.createForMetrics(resourceUrl, "language");
          List<Resource> resources = sonar.findAll(query);
          if (null != resources && !resources.isEmpty()) {
            for (Resource resource : resources) {
              // find rule
              String language = resource.getLanguage();
              if (StringUtils.isNotBlank(language)) {
                RuleQuery ruleQuery = new RuleQuery(language);
                List<Rule> rules = sonar.findAll(ruleQuery);
                if (null != rules) {
                  for (Rule rule : rules) {
                    if (!ruleKeys.contains(rule.getKey())) {
                      ruleKeys.add(rule.getKey());
                      rulesResult.add(rule);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    // return all collected rules
    return rulesResult;
  }

  public void sync(Project project) {
    SonarViolationsProvider sonarViolationsProvider = ServiceManager.getService(project, SonarViolationsProvider.class);
    if (null != sonarViolationsProvider) {
      sonarViolationsProvider.syncWithSonar(project);
    }
    SonarRulesProvider sonarRulesProvider = ServiceManager.getService(project, SonarRulesProvider.class);
    if (null != sonarRulesProvider) {
      sonarRulesProvider.syncWithSonar(project);
    }
  }

  public ProblemHighlightType sonarSeverityToProblemHighlightType(String sonarSeverity) {
    if (StringUtils.isBlank(sonarSeverity)) {
      return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    } else {
      sonarSeverity = sonarSeverity.toUpperCase();
      if (SonarSeverity.BLOCKER.toString().equals(sonarSeverity)) {
        return ProblemHighlightType.ERROR;
      } else if (SonarSeverity.CRITICAL.toString().equals(sonarSeverity) || SonarSeverity.MAJOR.toString().equals(sonarSeverity)) {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
      } else if (SonarSeverity.INFO.toString().equals(sonarSeverity) || SonarSeverity.MINOR.toString().equals(sonarSeverity)) {
        return ProblemHighlightType.WEAK_WARNING;
      } else {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
      }
    }
  }

  //TODO getAllProjects and subModules
}
