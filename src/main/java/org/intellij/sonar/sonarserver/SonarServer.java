package org.intellij.sonar.sonarserver;

import com.google.common.collect.ImmutableList;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.intellij.sonar.configuration.SonarQualifier;
import org.intellij.sonar.persistence.Resource;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.util.ProgressIndicatorUtil;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.Rules;
import org.sonarqube.ws.WsComponents;
import org.sonarqube.ws.WsComponents.Component;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.component.TreeWsRequest;
import org.sonarqube.ws.client.issue.IssuesService;
import org.sonarqube.ws.client.issue.SearchWsRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class SonarServer {

    private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 60*1000;
    private static final int READ_TIMEOUT_IN_MILLISECONDS = 60*1000;
    private static final int DOWNLOAD_LIMIT = 10_000;
    private final WsClient sonarClient;

    private SonarServer(SonarServerConfig sonarServerConfig) {
        this.sonarClient = SonarClientFactory.getInstance()
                .connectTimeoutInMs(CONNECT_TIMEOUT_IN_MILLISECONDS)
                .readTimeoutInMs(READ_TIMEOUT_IN_MILLISECONDS)
                .sonarServerConfig(sonarServerConfig)
                .createSonarClient();
    }

    public static SonarServer create(SonarServerConfig sonarServerConfigBean) {
        return new SonarServer(sonarServerConfigBean);
    }

    public Rule getRule(String key) {
        Rules.Rule rule = sonarClient.rules().show(null, key).getRule();
        return new Rule(rule.getKey(), rule.getName(), rule.getSeverity(), rule.getLang(), rule.getLangName(), rule.getHtmlDesc());
    }

    public List<Resource> getAllProjectsAndModules(String projectNameFilter, String organization) {
        List<Resource> allResources = new LinkedList<>();

        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText("Downloading SonarQube projects");
        List<Component> projects = getAllProjects(sonarClient, projectNameFilter, organization);
        projects = projects.stream().sorted(comparing(Component::getName)).collect(toList());

        indicator.setText("Downloading SonarQube modules");
        int i = 0;
        for (Component project : projects) {
            if (indicator.isCanceled()) break;
            i++;
            indicator.setFraction(1.0 * i / projects.size());
            indicator.setText2(project.getName());
            allResources.add(new Resource(project.getKey(), project.getName(), project.getQualifier()));
            List<Component> modules = getAllModules(sonarClient, project.getKey());
            modules = modules.stream().sorted(comparing(Component::getName)).collect(toList());
            for (Component module : modules) {
                allResources.add(new Resource(module.getKey(), module.getName(), module.getQualifier()));
            }
        }
        return allResources;
    }

    private List<Component> getAllProjects(WsClient sonarClient, String projectNameFilter, String organization) {
        org.sonarqube.ws.client.component.SearchWsRequest query = new org.sonarqube.ws.client.component.SearchWsRequest()
                .setQualifiers(singletonList(SonarQualifier.PROJECT.getQualifier()))
                .setPageSize(500); //-1 is not allowed, neither int max. The limit is 500.

        addSearchParameter(projectNameFilter, query::setQuery);
        addSearchParameter(organization, query::setOrganization);

        List<WsComponents.Component> components = new ArrayList<>();

        WsComponents.SearchWsResponse response = sonarClient.components().search(query);
        Common.Paging paging = response.getPaging();
        int total = paging.getTotal();
        int pageSize = paging.getPageSize();
        int pages = total / pageSize + (total % pageSize > 0 ? 1 : 0);

        if (total > 0)
        {
            components.addAll(response.getComponentsList());
            for (int pageIndex = 2; pageIndex <= pages; pageIndex++)
            {
                query.setPage(pageIndex);
                response = sonarClient.components().search(query);
                components.addAll(response.getComponentsList());
            }
        }

        return Collections.unmodifiableList(components);
    }

    private List<Component> getAllModules(WsClient sonarClient, String projectResourceKey) {
        TreeWsRequest query = new TreeWsRequest()
                .setQualifiers(singletonList(SonarQualifier.MODULE.getQualifier()))
                .setComponent(projectResourceKey);
        return sonarClient.components().tree(query).getComponentsList();
    }

    public ImmutableList<Issue> getAllIssuesFor(String resourceKey, String organization) {
        final ImmutableList.Builder<Issue> builder = ImmutableList.builder();
        SearchWsRequest query = new SearchWsRequest();
        query.setComponentRoots(singletonList(resourceKey))
                .setResolved(false)
                .setPageSize(500);
        query.setProjectKeys(singletonList(resourceKey));
        addSearchParameter(organization, query::setOrganization);
        IssuesService issuesService = sonarClient.issues();
        SearchWsResponse response = issuesService.search(query);
        builder.addAll(response.getIssuesList());
        Common.Paging paging = response.getPaging();
        showWarningIfDownloadLimitReached(paging);
        Integer total = Math.min(paging.getTotal(), DOWNLOAD_LIMIT);
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

    private void showWarningIfDownloadLimitReached(Common.Paging paging) {
        if (paging.getTotal() > DOWNLOAD_LIMIT) {
            Notifications.Bus.notify(new Notification(
                    "SonarQube","SonarQube",
                    String.format("Your project has %d issues, downloading instead the maximum amount of %s. ",
                            paging.getTotal(), DOWNLOAD_LIMIT),
                    NotificationType.WARNING
            ));
        }
    }

    private void addSearchParameter(String paramValue, Consumer<String> consumer) {
        if (paramValue != null && !"".equals(paramValue.trim())) {
            consumer.accept(paramValue.trim());
        }
    }
}
