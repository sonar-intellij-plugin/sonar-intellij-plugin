package org.intellij.sonar.sonarserver;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.removeEnd;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.ssl.CertificateManager;
import com.intellij.util.net.ssl.ConfirmingTrustManager;
import com.intellij.util.proxy.CommonProxy;

import org.apache.commons.lang.StringUtils;
import org.intellij.sonar.configuration.SonarQualifier;
import org.intellij.sonar.persistence.Resource;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.util.ProgressIndicatorUtil;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.Rules;
import org.sonarqube.ws.WsComponents.Component;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.component.TreeWsRequest;
import org.sonarqube.ws.client.issue.IssuesService;
import org.sonarqube.ws.client.issue.SearchWsRequest;

public class SonarServer {

    private static final Logger LOG = Logger.getInstance(SonarServer.class);

    private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 60*1000;
    private static final int READ_TIMEOUT_IN_MILLISECONDS = 60*1000;
    private final SonarServerConfig mySonarServerConfig;
    private final WsClient sonarClient;

    private SonarServer(SonarServerConfig sonarServerConfigBean) {
        this.mySonarServerConfig = sonarServerConfigBean;
        this.sonarClient = createSonarClient();
    }

    public static SonarServer create(SonarServerConfig sonarServerConfigBean) {
        return new SonarServer(sonarServerConfigBean);
    }

    private WsClient createSonarClient() {
        String hostUrl = removeEnd(mySonarServerConfig.getHostUrl(), "/");
        CertificateManager certificateManager = CertificateManager.getInstance();

        HttpConnector.Builder connectorBuilder = HttpConnector.newBuilder()
                .readTimeoutMilliseconds(READ_TIMEOUT_IN_MILLISECONDS)
                .connectTimeoutMilliseconds(CONNECT_TIMEOUT_IN_MILLISECONDS)
                .url(hostUrl)
                .setTrustManager(ConfirmingTrustManager.createForStorage(certificateManager.getCacertsPath(), certificateManager.getPassword()));

        if (!mySonarServerConfig.isAnonymous()) {
            if (StringUtils.isNotBlank(mySonarServerConfig.loadToken())) {
                // https://sonarqube.com/api/user_tokens/search
                connectorBuilder.token(mySonarServerConfig.getToken());
                mySonarServerConfig.clearToken();
            } else {
                mySonarServerConfig.loadPassword();
                connectorBuilder.credentials(mySonarServerConfig.getUser(), mySonarServerConfig.getPassword());
                mySonarServerConfig.clearPassword();
            }
        }

        Optional<Proxy> proxy = getIntelliJProxyFor(hostUrl);
        if (proxy.isPresent()) {
            HttpConfigurable proxySettings = HttpConfigurable.getInstance();
            connectorBuilder.proxy(proxy.get());
            if (proxySettings.PROXY_AUTHENTICATION) {
                connectorBuilder.proxyCredentials(proxySettings.getProxyLogin(), proxySettings.getPlainProxyPassword());
            }
        }

        return WsClientFactories.getDefault().newClient(connectorBuilder.build());
    }

    private Optional<Proxy> getIntelliJProxyFor(String server) {
        List<Proxy> proxies;
        try {
            proxies = CommonProxy.getInstance().select(new URL(server));
        } catch (MalformedURLException e) {
            LOG.error("Unable to configure proxy", e);
            return Optional.empty();
        }
        for (Proxy proxy : proxies) {
            if (proxy.type() == Proxy.Type.HTTP) {
                return Optional.of(proxy);
            }
        }
        return Optional.empty();
    }

    public Rule getRule(String key) {
        Rules.Rule rule = sonarClient.rules().show(null, key).getRule();
        return new Rule(rule.getKey(), rule.getName(), rule.getSeverity(), rule.getLang(), rule.getLangName(), rule.getHtmlDesc());
    }

    public List<Resource> getAllProjectsAndModules() {
        List<Resource> allResources = new LinkedList<>();
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText("Downloading SonarQube projects");
        List<Component> projects = getAllProjects(sonarClient);
        projects = projects.stream().sorted(comparing(Component::getName)).collect(toList());

        if (null != projects) {
            indicator.setText("Downloading SonarQube modules");
            int i = 0;
            for (Component project : projects) {
                if (indicator.isCanceled()) break;
                i++;
                indicator.setFraction(1.0 * i / projects.size());
                indicator.setText2(project.getName());
                allResources.add(new Resource(project.getKey(), project.getName(), project.getQualifier()));
                List<Component> modules = getAllModules(sonarClient, project.getId());
                modules = modules.stream().sorted(comparing(Component::getName)).collect(toList());
                if (null != modules) {
                    for (Component module : modules) {
                        allResources.add(new Resource(module.getKey(), module.getName(), module.getQualifier()));
                    }
                }
            }
        }
        return allResources;
    }

    public List<Component> getAllProjects(WsClient sonarClient) {
        org.sonarqube.ws.client.component.SearchWsRequest query = new org.sonarqube.ws.client.component.SearchWsRequest()
                .setQualifiers(singletonList(SonarQualifier.PROJECT.getQualifier()))
                .setPageSize(10000); //-1 is not allowed, neither int max. The limit is 10000.
        return sonarClient.components().search(query).getComponentsList();
    }

    public List<Component> getAllModules(WsClient sonarClient, String projectResourceId) {
        TreeWsRequest query = new TreeWsRequest()
                .setQualifiers(singletonList(SonarQualifier.MODULE.getQualifier()))
                .setBaseComponentId(projectResourceId);
        return sonarClient.components().tree(query).getComponentsList();
    }

    public ImmutableList<Issue> getAllIssuesFor(String resourceKey) {
        final ImmutableList.Builder<Issue> builder = ImmutableList.builder();
        SearchWsRequest query = new SearchWsRequest();
        query.setComponentRoots(singletonList(resourceKey))
                .setResolved(false)
                .setPageSize(-1);
        IssuesService issuesService = sonarClient.issues();
        SearchWsResponse response = issuesService.search(query);
        builder.addAll(response.getIssuesList());
        Common.Paging paging = response.getPaging();
        Integer total = paging.getTotal();
        Integer pageSize = paging.getPageSize();
        Integer pages = total / pageSize + (total % pageSize > 0 ? 1 : 0);
        for (int pageIndex = 2; pageIndex <= pages; pageIndex++) {
            final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
            if (progressIndicator.isCanceled())
                break;
            final String pagesProgressMessage = String.format("%d / %d pages downloaded", pageIndex, pages);
            ProgressIndicatorUtil.setText(progressIndicator, pagesProgressMessage);
            ProgressIndicatorUtil.setFraction(progressIndicator, pageIndex * 1.0 / pages);

            query.setPage(pageIndex);
            builder.addAll(issuesService.search(query).getIssuesList());
        }
        return builder.build();
    }
}
