package org.intellij.sonar.console;

import static org.intellij.sonar.console.ConsoleLogLevel.ERROR;
import static org.intellij.sonar.console.ConsoleLogLevel.INFO;

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

public class SonarConsole extends AbstractProjectComponent {

  private ConsoleView consoleView;
  private String passwordFiler;

  public SonarConsole(Project project) {
    super(project);
  }

  public static synchronized SonarConsole get(Project project) {
    return project.getComponent(SonarConsole.class);
  }

  public SonarConsole withPasswordFilter(String password) {
    this.passwordFiler = password;
    return this;
  }

  public void clearPasswordFilter() {
    this.passwordFiler = null;
  }

  private ConsoleView createConsoleView(Project project) {
    final ConsoleView newConsoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
    final ToolWindow toolWindow = ToolWindowManager.getInstance(project)
      .getToolWindow(SonarToolWindowFactory.TOOL_WINDOW_ID);
    ApplicationManager.getApplication().invokeLater(
      new Runnable() {
        @Override
        public void run() {
          toolWindow.show(
            new Runnable() {
              @Override
              public void run() {
                Content content = toolWindow.getContentManager()
                  .getFactory()
                  .createContent(newConsoleView.getComponent(),"SonarQube Console",true);
                toolWindow.getContentManager().addContent(content);
              }
            }
          );
        }
      }
    );
    return newConsoleView;
  }

  public void log(String msg,ConsoleLogLevel logLevel) {
    msg = applyPasswordFilter(msg);
    final ConsoleViewContentType consoleViewContentType;
    if (logLevel == INFO) {
      consoleViewContentType = ConsoleViewContentType.NORMAL_OUTPUT;
    } else
      if (logLevel == ERROR) {
        consoleViewContentType = ConsoleViewContentType.ERROR_OUTPUT;
      } else {
        throw new IllegalArgumentException(String.format("Unknown log level %s",logLevel));
      }
    getConsoleView().print(String.format("%s %s > %s\n",logLevel,getNowFormatted(),msg),consoleViewContentType);
  }

  private String applyPasswordFilter(String msg) {
    if (null != passwordFiler && null != msg && msg.contains(passwordFiler)) {
      msg = msg.replace(passwordFiler,"●●●●●●●");
    }
    return msg;
  }

  public void info(String msg) {
    log(msg,INFO);
  }

  public void error(String msg) {
    log(msg,ERROR);
  }

  private String getNowFormatted() {
    return DateTimeFormat.forPattern("HH:mm:ss.SSS").print(DateTime.now());
  }

  public void clear() {
    getConsoleView().clear();
  }

  private synchronized ConsoleView getConsoleView() {
    if (this.consoleView == null) {
      this.consoleView = createConsoleView(myProject);
    }
    return this.consoleView;
  }
}
