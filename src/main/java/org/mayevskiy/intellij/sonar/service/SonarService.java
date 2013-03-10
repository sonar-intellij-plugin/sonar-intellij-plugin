package org.mayevskiy.intellij.sonar.service;

import org.mayevskiy.intellij.sonar.bean.SonarSettingsBean;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Violation;
import org.sonar.wsclient.services.ViolationQuery;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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
        Sonar sonar = Sonar.create(sonarSettingsBean.host, sonarSettingsBean.user, sonarSettingsBean.password);
        ViolationQuery violationQuery = ViolationQuery.createForResource(sonarSettingsBean.resource);
        violationQuery.setDepth(-1);
        violationQuery.setSeverities("BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO");

        return sonar.findAll(violationQuery);
    }
}
