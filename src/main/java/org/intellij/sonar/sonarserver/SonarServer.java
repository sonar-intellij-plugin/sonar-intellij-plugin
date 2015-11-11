package org.intellij.sonar.sonarserver;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.proxy.CommonProxy;
import org.apache.commons.lang.StringUtils;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.util.ProgressIndicatorUtil;
import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.SonarClient;
import org.sonar.wsclient.base.Paging;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonar.wsclient.issue.Issues;
import org.sonar.wsclient.services.*;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SonarServer {

    private static final Logger LOG = Logger.getInstance(SonarServer.class);
    public static class RuleWrapper {
        public Rule rule; // { "rule": {...} }
    }

    interface Rules {
        @GET("/api/rules/show")
        RuleWrapper show(
                @Query("key") String key,
                @Query("actives") Boolean actives
        );
    }
    private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 60*1000;
    private static final int READ_TIMEOUT_IN_MILLISECONDS = 60*1000;
    private final SonarServerConfig mySonarServerConfig;
    private final Sonar sonar;
    private final SonarClient sonarClient;

    private SonarServer(SonarServerConfig sonarServerConfigBean) {
        this.mySonarServerConfig = sonarServerConfigBean;
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
                .readTimeoutMilliseconds(READ_TIMEOUT_IN_MILLISECONDS)
                .connectTimeoutMilliseconds(CONNECT_TIMEOUT_IN_MILLISECONDS)
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
        Sonar newSonar;
        if (mySonarServerConfig.isAnonymous()) {
            newSonar = createSonar(mySonarServerConfig.getHostUrl(), null, null);
        } else {
            mySonarServerConfig.loadPassword();
            newSonar = createSonar(mySonarServerConfig.getHostUrl(), mySonarServerConfig.getUser(), mySonarServerConfig.getPassword());
            mySonarServerConfig.clearPassword();
        }
        return newSonar;
    }

    private Host createHost() {
        Host host;
        final String safeHostUrl = getHostSafe(mySonarServerConfig.getHostUrl());
        if (mySonarServerConfig.isAnonymous()) {
            host = new Host(safeHostUrl);
        } else {
            mySonarServerConfig.loadPassword();
            host = new Host(safeHostUrl, mySonarServerConfig.getUser(), mySonarServerConfig.getPassword());
            mySonarServerConfig.clearPassword();
        }
        return host;
    }

    private Sonar createSonar(String host, String user, String password) {
        String safeHost = getHostSafe(host);
        return StringUtils.isEmpty(user) ? Sonar.create(safeHost) : Sonar.create(safeHost, user, password);
    }

    private String getHostSafe(String hostName) {
        return StringUtils.removeEnd(hostName, "/");
    }

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
        query.setTimeoutMilliseconds(READ_TIMEOUT_IN_MILLISECONDS);
        return sonar.find(query);
    }

// GET LIST OF RULES FOR A SONAR PROFILE language is mandatory!
//  https://sonar.corp.mobile.de/sonar/api/profiles?language=java&name=mobile_relaxed&format=json

    /**
     * @param language    like java
     * @param profileName like Sonar Way
     * @return Quality profile containing enabled rules
     */
    public Profile getProfile(String language, String profileName) {
        ProfileQuery query = ProfileQuery.create(language, profileName);
        query.setTimeoutMilliseconds(READ_TIMEOUT_IN_MILLISECONDS);
        return sonar.find(query);
    }

    // https://sonar.corp.mobile.de/sonar/api/rules?language=java&format=json
    // Unfortunately profile query contains neither rule title nor rule description

    /**
     * @param language like java
     * @return list of all rules for a language
     */
    public List<org.sonar.wsclient.services.Rule> getRules(String language) {
        RuleQuery query = new RuleQuery(language);
        query.setTimeoutMilliseconds(READ_TIMEOUT_IN_MILLISECONDS);
        return sonar.findAll(query);
    }

    public Rule getRule(String key) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(mySonarServerConfig.getHostUrl())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        final Rules rules = restAdapter.create(Rules.class);
        final Rule rule = rules.show(key, null).rule;
        return rule;
    }

    public List<Resource> getAllProjectsAndModules() {
        List<Resource> allResources = new LinkedList<Resource>();
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText("Downloading SonarQube projects");
        List<Resource> projects = getAllProjects(sonar);
        projects = FluentIterable.from(projects).toSortedList(new ByResourceName());

        if (null != projects) {
            indicator.setText("Downloading SonarQube modules");
            int i = 0;
            for (Resource project : projects) {
                if (indicator.isCanceled()) break;
                i++;
                indicator.setFraction(1.0 * i / projects.size());
                indicator.setText2(project.getName());
                allResources.add(project);
                List<Resource> modules = getAllModules(sonar, project.getId());
                modules = FluentIterable.from(modules).toSortedList(new ByResourceName());
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
        query.setTimeoutMilliseconds(READ_TIMEOUT_IN_MILLISECONDS);
        return sonar.findAll(query);
    }

    public List<Resource> getAllModules(Sonar sonar, Integer projectResourceId) {
        ResourceQuery query = new ResourceQuery(projectResourceId);
        query.setDepth(-1);
        query.setQualifiers(Resource.QUALIFIER_MODULE);
        query.setTimeoutMilliseconds(READ_TIMEOUT_IN_MILLISECONDS);
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
        Paging paging = issues.paging();
        Integer pages = paging.pages();
        Integer total = paging.total();
        Integer pageSize = paging.pageSize();
        if (pages == null) {
            pages = total / pageSize + (total % pageSize > 0 ? 1 : 0);
        }
        if (pages != null) {
            for (int pageIndex = 2; pageIndex <= pages; pageIndex++) {
                final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
                if (progressIndicator.isCanceled())
                    break;
                final String pagesProgressMessage = String.format("%d / %d pages downloaded", pageIndex, pages);
                ProgressIndicatorUtil.setText(progressIndicator, pagesProgressMessage);
                ProgressIndicatorUtil.setFraction(progressIndicator, pageIndex * 1.0 / pages);

                query = IssueQuery.create()
                        .componentRoots(resourceKey)
                        .resolved(false)
                        .pageSize(-1)
                        .pageIndex(pageIndex);
                issues = sonarClient.issueClient().find(query);
                builder.addAll(issues.list());
            }
        }
        return builder.build();
    }

    private static class ByResourceName implements Comparator<Resource> {
        @Override
        public int compare(Resource resource, Resource resource2) {
            return resource.getName().compareTo(resource2.getName());
        }
    }
}
