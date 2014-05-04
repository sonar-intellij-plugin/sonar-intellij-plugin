package org.intellij.sonar.console;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.swing.*;

import static org.intellij.sonar.console.ConsoleLogLevel.ERROR;
import static org.intellij.sonar.console.ConsoleLogLevel.INFO;

public class SonarConsole extends AbstractProjectComponent {

  private ConsoleView consoleView;

  public SonarConsole(Project project) {
    super(project);
  }

  public static synchronized SonarConsole get(Project project) {
    return project.getComponent(SonarConsole.class);
  }

  private ConsoleView createConsoleView(Project project) {
    final ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
    final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(SonarToolWindowFactory.TOOL_WINDOW_ID);
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        toolWindow.show(new Runnable() {
          @Override
          public void run() {
            Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "SonarQube Console", true);
            toolWindow.getContentManager().addContent(content);
          }
        });
      }
    });
    return consoleView;
  }

  public void log(String msg, ConsoleLogLevel logLevel) {
    if (INFO == logLevel) {
      info(msg);
    } else if (ERROR == logLevel) {
      error(msg);
    } else {
      throw new IllegalArgumentException(String.format("Unknown log level %s", logLevel));
    }
  }

  public void info(String msg) {
    getConsoleView().print(String.format("%s %s > %s\n", INFO, getNowFormatted(), msg), ConsoleViewContentType.NORMAL_OUTPUT);
  }

  public void error(String msg) {
    getConsoleView().print(String.format("%s %s > %s\n", ERROR, getNowFormatted(), msg), ConsoleViewContentType.ERROR_OUTPUT);
  }

  private String getNowFormatted() {
    return DateTimeFormat.forPattern("HH:mm:ss.SSS").print(DateTime.now());
  }

  private synchronized ConsoleView getConsoleView() {
    if (this.consoleView == null) {
      this.consoleView = createConsoleView(myProject);
    }
    return this.consoleView;
  }
}
