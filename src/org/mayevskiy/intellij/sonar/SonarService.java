package org.mayevskiy.intellij.sonar;

import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.connectors.Connector;
import org.sonar.wsclient.connectors.HttpClient4Connector;
import org.sonar.wsclient.services.CreateQuery;
import org.sonar.wsclient.services.DeleteQuery;
import org.sonar.wsclient.services.Query;
import org.sonar.wsclient.services.UpdateQuery;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SonarService  {
    public boolean testConnection(String host){
        try {
            Host server = new Host(host);
            Connector connector = new HttpClient4Connector(server);
            Sonar sonar = new Sonar(connector);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
