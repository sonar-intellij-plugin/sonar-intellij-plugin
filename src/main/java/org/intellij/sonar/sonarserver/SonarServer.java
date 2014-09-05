package org.intellij.sonar.sonarserver;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.proxy.CommonProxy;
import org.apache.commons.lang.StringUtils;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.util.GuaveStreamUtil;
import org.intellij.sonar.util.ThrowableUtils;
import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.SonarClient;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonar.wsclient.issue.Issues;
import org.sonar.wsclient.services.*;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class SonarServer {

  private static final Logger LOG = Logger.getInstance(SonarServer.class);

  private static final String VERSION_URL = "/api/server/version";
  private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 3000;
  private static final int READ_TIMEOUT_IN_MILLISECONDS = 6000;
  private static final String USER_AGENT = "SonarQube Community Plugin";
  public static final int READ_TIMEOUT = 5000;

  private final SonarServerConfig mySonarServerConfigBean;
  private final Sonar sonar;
  private final SonarClient sonarClient;

  private SonarServer(SonarServerConfig sonarServerConfigBean) {
    this.mySonarServerConfigBean = sonarServerConfigBean;
    this.sonar = createSonar();
    this.sonarClient = createSonarClient(createHost());
  }

  public static SonarServer create(String hostUrl) {
    return create(SonarServerConfig.of(hostUrl));
  }

  public static SonarServer create(SonarServerConfig sonarServerConfigBean) {
    return new SonarServer(sonarServerConfigBean);
  }

  private SonarClient createSonarClient(Host host) {
    SonarClient.Builder builder = SonarClient.builder()
        .readTimeoutMilliseconds(READ_TIMEOUT)
        .url(host.getHost())
        .login(host.getUsername())
        .password(host.getPassword());
    Optional<Proxy> proxy = getIntelliJProxyFor(host);
    if (proxy.isPresent()) {
      InetSocketAddress address = (InetSocketAddress) proxy.get().address();
      HttpConfigurable proxySettings = HttpConfigurable.getInstance();
      builder.proxy(address.getHostName(), address.getPort());
      if (proxySettings.PROXY_AUTHENTICATION) {
        builder.proxyLogin(proxySettings.PROXY_LOGIN).proxyPassword(proxySettings.getPlainProxyPassword());
      }
    }
    return builder.build();
  }

  private Optional<Proxy> getIntelliJProxyFor(Host server) {
    List<Proxy> proxies;
    try {
      proxies = CommonProxy.getInstance().select(new URL(server.getHost()));
    } catch (MalformedURLException e) {
      LOG.error("Unable to configure proxy", e);
      return Optional.absent();
    }
    for (Proxy proxy : proxies) {
      if (proxy.type() == Proxy.Type.HTTP) {
        return Optional.of(proxy);
      }
    }
    return Optional.absent();
  }

  private Sonar createSonar() {
    Sonar sonar;
    if (mySonarServerConfigBean.isAnonymous()) {
      sonar = createSonar(mySonarServerConfigBean.getHostUrl(), null, null);
    } else {
      mySonarServerConfigBean.loadPassword();
      sonar = createSonar(mySonarServerConfigBean.getHostUrl(), mySonarServerConfigBean.getUser(), mySonarServerConfigBean.getPassword());
      mySonarServerConfigBean.clearPassword();
    }
    return sonar;
  }

  private Host createHost() {
    Host host;
    final String safeHostUrl = getHostSafe(mySonarServerConfigBean.getHostUrl());
    if (mySonarServerConfigBean.isAnonymous()) {
      host = new Host(safeHostUrl);
    } else {
      mySonarServerConfigBean.loadPassword();
      host = new Host(safeHostUrl, mySonarServerConfigBean.getUser(), mySonarServerConfigBean.getPassword());
      mySonarServerConfigBean.clearPassword();
    }
    return host;
  }

  private Sonar createSonar(String host, String user, String password) {
    host = getHostSafe(host);
    return StringUtils.isEmpty(user) ? Sonar.create(host) : Sonar.create(host, user, password);
  }

  private String getHostSafe(String hostName) {
    return StringUtils.removeEnd(hostName, "/");
  }

  public SonarServerConfig getSonarServerConfigurationBean() {
    return mySonarServerConfigBean;
  }

  public String verifySonarConnection() throws SonarServerConnectionException {
    HttpURLConnection httpURLConnection = getHttpConnection();

    try {
      int statusCode = httpURLConnection.getResponseCode();
      if (statusCode != HttpURLConnection.HTTP_OK) {
        throw new SonarServerConnectionException("ResponseCode: %d Url: %s", statusCode, httpURLConnection.getURL());
      }
      return GuaveStreamUtil.toString(httpURLConnection.getInputStream());
    } catch (IOException e) {
      throw new SonarServerConnectionException("Cannot read data from url: %s\n\n Cause: \n%s", httpURLConnection.getURL(), ThrowableUtils.getPrettyStackTraceAsString(e));
    }
  }

  private HttpURLConnection getHttpConnection() throws SonarServerConnectionException {
    String hostName = mySonarServerConfigBean.getHostUrl();
    URL sonarServerUrl = null;
    try {
      sonarServerUrl = new URL(getHostSafe(hostName) + VERSION_URL);

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

  /*@NotNull
  public List<Violation> getViolations(SonarSettingsBean sonarSettingsBean) {
    if (null == sonarSettingsBean) {
      return Collections.emptyList();
    }
    final Sonar sonar = createSonar(sonarSettingsBean);
    ViolationQuery violationQuery = ViolationQuery
        .createForResource(sonarSettingsBean.resource)
        .setDepth(-1)
        .setSeverities("BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO");

    return sonar.findAll(violationQuery);
  }*/



  /*public Sonar createSonar(SonarSettingsBean sonarSettingsBean) {
    return createSonar(sonarSettingsBean.host, sonarSettingsBean.user, sonarSettingsBean.password);
  }*/

  /*public ImmutableSet<Rule> getRulesFor(SonarServerConfigurationBean configurationBean, Collection<String> sonarResources) {
    Sonar sonar = createSonar(configurationBean);

    for (String resource: sonarResources) {
      final ResourceQuery query = ResourceQuery.createForMetrics(resource, "rule");
      final Resource result = sonar.find(query);
      System.out.println(result);
    }
  }*/

// GET LANGUAGE AND RULES PROFILE FOR A SONAR RESOURCE
//  https://sonar.corp.mobile.de/sonar/api/resources?format=json&resource=autoact:autoact-b2b-api_groovy&metrics=profile

  // Set<language,profile> s= new Set;
  // for resource in resources:
  //   s.put( resource.language, resource.profile )

  // for entry in s:
  //   getRulesFor(entry.language, entry.profile)

  /**
   * <pre>
   * Usage: <br>
   * {@code
   * Resource resource = getResourceWithProfile(sonar, resourceKey);
   * String profile = resource.getMeasure("profile").getData();
   * }
   * </pre>
   *
   * @param resourceKey like sonar:project
   */
  public Resource getResourceWithProfile(String resourceKey) {
    final ResourceQuery query = ResourceQuery.createForMetrics(resourceKey, "profile");
    query.setTimeoutMilliseconds(READ_TIMEOUT);
    return sonar.find(query);
  }

// GET LIST OF RULES FOR A SONAR PROFILE language is mandatory!
//  https://sonar.corp.mobile.de/sonar/api/profiles?language=java&name=mobile_relaxed&format=json
  /**
   * @param language like java
   * @param profileName like Sonar Way
   * @return Quality profile containing enabled rules
   */
  public Profile getProfile(String language, String profileName) {
    ProfileQuery query = ProfileQuery.create(language, profileName);
    query.setTimeoutMilliseconds(READ_TIMEOUT);
    return sonar.find(query);
  }

  // https://sonar.corp.mobile.de/sonar/api/rules?language=java&format=json
  // Unfortunately profile query contains neither rule title nor rule description
  /**
   * @param language like java
   * @return list of all rules for a language
   */
  public List<Rule> getRules(String language) {
    RuleQuery query = new RuleQuery(language);
    query.setTimeoutMilliseconds(READ_TIMEOUT);
    return sonar.findAll(query);
  }

 /* public Collection<Rule> getAllRules(Collection<SonarSettingsBean> sonarSettingsBeans, @NotNull ProgressIndicator indicator) {
    List<Rule> rulesResult = new LinkedList<Rule>();
    Set<String> ruleKeys = new LinkedHashSet<String>();
    for (SonarSettingsBean sonarSettingsBean : sonarSettingsBeans) {
      indicator.checkCanceled();

      final Sonar sonar = createSonar(sonarSettingsBean);

      // for all SettingsBeans do:  find language
      String resourceUrl = sonarSettingsBean.resource;
      if (StringUtils.isNotBlank(resourceUrl)) {
        ResourceQuery query = ResourceQuery.createForMetrics(resourceUrl, "language");
        List<Resource> resources = sonar.findAll(query);
        if (null != resources && !resources.isEmpty()) {
          for (Resource resource : resources) {
            indicator.checkCanceled();

            // find rule
            String language = resource.getLanguage();
            if (StringUtils.isNotBlank(language)) {
              RuleQuery ruleQuery = new RuleQuery(language);
              List<Rule> rules = sonar.findAll(ruleQuery);
              if (null != rules) {
                for (Rule rule : rules) {
                  indicator.checkCanceled();

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
    // return all collected rules
    return rulesResult;
  }
*/


  public List<Resource> getAllProjectsAndModules() {
    List<Resource> allResources = new LinkedList<Resource>();
    List<Resource> projects = getAllProjects(sonar);
    if (null != projects) {
      for (Resource project : projects) {
        allResources.add(project);
        List<Resource> modules = getAllModules(sonar, project.getId());
        if (null != modules) {
          for (Resource module : modules) {
            allResources.add(module);
          }
        }
      }
    }
    return allResources;
  }

  public List<Resource> getAllProjects(Sonar sonar) {
    ResourceQuery query = new ResourceQuery();
    query.setQualifiers(Resource.QUALIFIER_PROJECT);
    query.setTimeoutMilliseconds(READ_TIMEOUT);
    return sonar.findAll(query);
  }

  public List<Resource> getAllModules(Sonar sonar, Integer projectResourceId) {
    ResourceQuery query = new ResourceQuery(projectResourceId);
    query.setDepth(-1);
    query.setQualifiers(Resource.QUALIFIER_MODULE);
    query.setTimeoutMilliseconds(READ_TIMEOUT);
    return sonar.findAll(query);
  }

  public Issues getIssuesFor(String resourceKey) {
    IssueQuery query = IssueQuery.create()
        .componentRoots(resourceKey)
        .resolved(false)
        .pageSize(-1);
    return sonarClient.issueClient().find(query);
  }

  public ImmutableList<Issue> getAllIssuesFor(String resourceKey) {
    final ImmutableList.Builder<Issue> builder = ImmutableList.builder();
    IssueQuery query = IssueQuery.create()
        .componentRoots(resourceKey)
        .resolved(false)
        .pageSize(-1);
    Issues issues = sonarClient.issueClient().find(query);
    builder.addAll(issues.list());
    for (int i=2; i <= issues.paging().pages(); i++) {
       query = IssueQuery.create()
          .componentRoots(resourceKey)
          .resolved(false)
          .pageSize(-1)
          .pageIndex(i);
      issues = sonarClient.issueClient().find(query);
      builder.addAll(issues.list());
    }
    return builder.build();
  }

}
