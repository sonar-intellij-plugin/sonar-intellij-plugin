package org.mayevskiy.intellij.sonar;

import org.apache.commons.lang.StringUtils;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SonarService {

    private final static String VERSION_URL = "/api/server/version";
    public static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 3000;
    public static final int READ_TIMEOUT_IN_MILLISECONDS = 6000;
    public static final String USER_AGENT = "Sonar IntelliJ Connector";

    private HttpURLConnection getHttpConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS);
        connection.setReadTimeout(READ_TIMEOUT_IN_MILLISECONDS);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        return connection;
    }

    public String testConnectionByGettingSonarServerVersion(SonarSettingsBean sonarSettingsBean) throws Exception {
        String fullUrl = sonarSettingsBean.host + VERSION_URL;
        HttpURLConnection httpURLConnection = getHttpConnection(new URL(fullUrl));
        int statusCode = httpURLConnection.getResponseCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new Exception();
        }
        Reader reader = new InputStreamReader((InputStream) httpURLConnection.getContent());
        BufferedReader bufferedReader = new BufferedReader(reader);

        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (null != line) {
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }

        return stringBuilder.toString();
    }

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

    public List<Rule> getAllRules(List<SonarSettingsBean> sonarSettingsBeans) {
        List<Rule> rulesResult = new LinkedList<>();
        Set<String> ruleKeys = new LinkedHashSet<>();
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

    //TODO getAllProjects and subModules
}
