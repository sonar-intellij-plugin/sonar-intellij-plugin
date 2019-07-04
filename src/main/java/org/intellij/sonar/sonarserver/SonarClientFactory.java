package org.intellij.sonar.sonarserver;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.ssl.CertificateManager;
import com.intellij.util.net.ssl.ConfirmingTrustManager;
import com.intellij.util.proxy.CommonProxy;
import org.apache.commons.lang.StringUtils;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang.StringUtils.removeEnd;

public class SonarClientFactory {

    private static final Logger LOG = Logger.getInstance(SonarClientFactory.class);
    private int connectTimeoutInMilliseconds;
    private int readTimeoutInMilliseconds;
    private SonarServerConfig sonarServerConfig;

    public static SonarClientFactory getInstance() {
        return new SonarClientFactory();
    }

    WsClient createSonarClient() {
        String hostUrl = removeEnd(sonarServerConfig.getHostUrl(), "/");
        CertificateManager certificateManager = CertificateManager.getInstance();

        HttpConnector.Builder connectorBuilder = HttpConnector.newBuilder()
                .connectTimeoutMilliseconds(connectTimeoutInMilliseconds)
                .readTimeoutMilliseconds(readTimeoutInMilliseconds)
                .url(hostUrl)
                .setTrustManager(ConfirmingTrustManager.createForStorage(certificateManager.getCacertsPath(), certificateManager.getPassword()));

        if (!sonarServerConfig.isAnonymous()) {
            if (StringUtils.isNotBlank(sonarServerConfig.loadToken())) {
                // https://sonarqube.com/api/user_tokens/search
                connectorBuilder.token(sonarServerConfig.getToken());
                sonarServerConfig.clearToken();
            } else {
                sonarServerConfig.loadPassword();
                connectorBuilder.credentials(sonarServerConfig.getUser(), sonarServerConfig.getPassword());
                sonarServerConfig.clearPassword();
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

    SonarClientFactory connectTimeoutInMs(int connectTimeoutInMilliseconds) {
        this.connectTimeoutInMilliseconds = connectTimeoutInMilliseconds;
        return this;
    }

    SonarClientFactory readTimeoutInMs(int readTimeoutInMilliseconds) {
        this.readTimeoutInMilliseconds = readTimeoutInMilliseconds;
        return this;
    }

    SonarClientFactory sonarServerConfig(SonarServerConfig sonarServerConfig) {
        this.sonarServerConfig = sonarServerConfig;
        return this;
    }
}
