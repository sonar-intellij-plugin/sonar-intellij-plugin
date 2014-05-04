package org.intellij.sonar.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {

  private final InputStream is;
  private final SonarConsole console;
  private final ConsoleLogLevel logLevel;

  public StreamGobbler(InputStream is, SonarConsole sonarConsole, ConsoleLogLevel logLevel) {
    this.is = is;
    this.console = sonarConsole;
    this.logLevel = logLevel;
  }

  public void run() {
    try {
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String line;
      while (!isInterrupted() && (line = br.readLine()) != null) {
        console.log(line, logLevel);
      }
    } catch (IOException ignore) {
      // do nothing if stream closed
    }
  }
}
