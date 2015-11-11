SonarQube IntelliJ Community Plugin
===================================

The main goal of this plugin is to show [SonarQube](http://sonarqube.org) issues directly within your IntelliJ IDE.
Currently the plugin is build to work in IntelliJ IDEA, RubyMine, WebStorm, PhpStorm, PyCharm, AppCode and Android Studio with any programming language you can analyze in SonarQube.

Two tasks are covered by the plugin: 
* downloading issues of previously analyzed code from a Sonar server and show them in your IDE
* running a script to perform a local analysis to find issues in your local code

We appreciate constructive feedback and contributions of any kind, so please report any issues with the plugin by [filing a new issue](https://github.com/sonar-intellij-plugin/sonar-intellij-plugin/issues/new), get in touch via our [Google Groups mailing list](https://groups.google.com/forum/#!forum/sonarqube-intellij-plugin) or send a pull request whenever you feel things could be done in a better way. We are really grateful for your support.


## Usage

### Project Configuration

You can install the "SonarQube Community Plugin" via the plugin manager inside your Jetbrains IDE or download it from the [Jetbrains Plugin Repository](http://plugins.jetbrains.com/plugin/7238). After the installation, you first of all need to configure the connection to your Sonar server. This is done per project and/ or module. You can use a remote server or a local one on your machine.

In your IDE go to `Preferences -> SonarQube`. 

![alt text][serverSelection]
[serverSelection]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/server_selection.png "Example server selection"


Click Add, enter the address of your Sonar server and the credentials (if needed) and click `OK`.

![alt text][serverConfiguration]
[serverConfiguration]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/server_configuration.png "Example server configuration"


Back on the previous screen, find the `Sonar resources` section and click the `+` button to select the Sonar resource for this project:

![alt text][resourceSelection]
[resourceSelection]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/resource_selection.png "Example resource selection"

Your final SonarQube Server configuration should now look like the following:

![alt text][serverConfigurationComplete]
[serverConfigurationComplete]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/server_configuration_complete.png "Example resource selection"


### Code inspection

The plugin provides two inspections:
* SonarQube - shows already analysed issues
* SonarQube (new issues) - shows only new issues from local analysis

To perform a code inspection you can:
Go to `Analyze -> Inspect code`.
Select whole project. It is recommended that you create a Sonar Inspection profile, with Sonar inspections only, but you can also use the default profile or any other self defined inspection profile.

After the execution the inspection result should look like:
![alt text][analysisResults]
[analysisResults]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/analysis_results.png "Example resource selection"

As the Sonar analysis process is prone to errors, it is essential to see what happened during the analysis. You can use the Sonar console for error analysis, especially during initial configuration:
![alt text][sonarConsole]
[sonarConsole]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/sonar_console2.png "Example resource selection"

### Local analysis configuration

After configuring the Sonar server you are ready to start downloading issues and showing them in the IDEA. But as soon you start editing your source code, you might want to trigger a local sonar analysis. To achieve this by using the plugin and showing new issues directly inside the IDEA, you need to tell the plugin how to analyse your project and provide the path to the sonar-report.json file. The plugin understands the contents of the report file and shows the results in IDEA like any other inspection.
Before configuring the plugin, you need to understand how to run local analysis for your project. 

Go to your preferred console and try to run depending on your project something like:

```
mvn sonar:sonar -DskipTests=true -Dsonar.language=java  -Dsonar.analysis.mode=incremental -Dsonar.host.url=http://localhost:9000
```

or

```
sonar-runner -Dsonar.analysis.mode=incremental -Dsonar.issuesReport.html.enable=true -Dsonar.host.url=http://localhost:9000
```

or
```
your_custom_script_to_perform_local_analysis.sh # may use gradle, ant, what ever else you prefer
```

After the script is done, your should see something like:

```
[INFO] [18:29:26.380] Export results to /path/to/your/project/target/sonar/sonar-report.json
[INFO] [18:29:26.383] Store results in database
[INFO] [18:29:26.500] ANALYSIS SUCCESSFUL
[INFO] [18:29:26.501] Executing post-job class org.sonar.issuesreport.ReportJob
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

This tells you where to find sonar-report.json. This file is essential, as it tells the plugin the location of the new issues.

**NOTE: The configuration of the local analysis is out of the scope of the plugin, please read the SonarQube documation about how to perform it**

After you know how to perform local analysis, you need to configure the plugin:

Go to `File -> Settings (Ctrl+Alt+S)-> SonarQube`.

![alt text][localScriptManagement]
[localScriptManagement]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/local_script_management.png "Example local script management"

Click add and define:

* a unique name, e.g. java-only
* the script itself, e.g. sonar-runner -Dsonar.analysis.mode=incremental ...
* path to the sonar-report.json

A finished configuration can look like:
![alt text][localScriptConfigured]
[localScriptConfigured]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/local_script_configured.png "Example local script management"

**NOTE: If command like "mvn" or "sonar-runner" does not work, you can use a full path instead: /path/to/mvn.**

#### Placeholders

In the previous example we have used a hard coded script and a sonar-report.json file path using a $WORKING_DIR placeholder. You can use several placeholders to replace values in your script or sonar-report.json file path.

placeholder | meaning
----------- | -------
`$WORKING_DIR` | the working directory of script executin, e.g. /path/to/project
`$WORKING_DIR_NAME` | the name of the working directory without the full path, e.g. project
`$MODULE_NAME` | the name of the module, e.g. example-java-maven
`$MODULE_BASE_DIR` | the directory of the .iml file, e.g. /path/to/project/module
`$MODULE_BASE_DIR_NAME` | the name of the directory of the .iml file, e.g. module
`$PROJECT_NAME` | the name of the project, e.g. my project
`$PROJECT_BASE_DIR` | the project root directory, e.g. /path/to/project
`$PROJECT_BASE_DIR_NAME` | the name of the project root directory, e.g. project
`$SONAR_HOST_URL` | the sonar host url, e.g. http://localhost:9000
`$SONAR_SERVER_NAME` | the sonar server name, e.g. my server
`$SONAR_USER_NAME` | the sonar user name, e.g. my_user
`$SONAR_USER_PASSWORD` | the sonar user password, e.g. pw

Using the placeholders you can define one script and reuse it in several projects. It is also useful if your project is a multi module project.
For example in a multi module mvn project you can define:

Script
```
/path/to/mvn sonar:sonar -DskipTests=true -Dsonar.language=java  -Dsonar.analysis.mode=incremental -Dsonar.host.url=$SONAR_HOST_URL
```

Path to sonar-report.json
```
$WORKING_DIR/target/sonar/sonar-report.json
```

If executing full project analysis, the plugin will do:

working dir
```
/path/to/project
```

script
```
/path/to/mvn sonar:sonar -DskipTests=true -Dsonar.language=java  -Dsonar.analysis.mode=incremental -Dsonar.host.url=http://localhost:9000
```

path to sonar-report.json
```
/path/to/project/target/sonar/sonar-report.json
```

And during analysis of a single module:

working dir
```
/path/to/project/module
```

script
```
/path/to/mvn sonar:sonar -DskipTests=true -Dsonar.language=java  -Dsonar.analysis.mode=incremental -Dsonar.host.url=http://localhost:9000
```

path to sonar-report.json
```
/path/to/project/mobule/target/sonar/sonar-report.json
```

**NOTE: `$WORKING_DIR` is replaced by `/path/to/project` for project and by `/path/to/project/module` for a module.**

**NOTE: if your module.iml files are not located in same directory as the module root, then you can override the working directory manually.**

### Module configuration

Module configuration is similar project configuration. **Please note that for a multi module maven project you need to manually define the sonar resource for each module.**

Go to `Project Structure -> Select a module -> Select SonarQube Tab`.

Configure the module in the same way as a project. You can use a special option `<PROJECT>`, in this case the project configuration will be used.
The local analysis script is per default, starting in the module base directory.

#### Example: A multi-module maven project

```
project
  module1
    pom.xml 
  module2
    pom.xml
  pom.xml <- parent
```

When you analyze `module1`, the plugin will download the issues for the sonar resource configured in the module settings for `module1` and start a local analysis script in `project/module1/`.

When you analyze the whole project, the plugin will download the issues for the sonar resource configured in the project settings and start a local analysis script in `project/`

You can use the same local script configuration for module or project level analysis:

- script: `up_to_you.sh $SONAR_HOST_URL`
- path to sonar-report.json: `$WORKING_DIR/target/sonar/sonar-report.json`

possible contents of the `up_to_you.sh` script:
```
#!/bin/bash
export JAVA_HOME="/path/to/jdk1.8.0.jdk/Home/"
export MAVEN_OPTS="-XX:MaxPermSize=128m"
/path/to/mvn sonar:sonar -DskipTests=true -Djava.awt.headless=true -Dsonar.language=java -Dsonar.analysis.mode=incremental -Dsonar.host.url=$1 -Dsonar.profile=your_java_profile
```

Tip: Omit the `sonar.language` parameter if you have multiple languages in your project (e.g. Java and Groovy).

## License

The project is licensed under Apache Public License 2.0! See the [LICENSE](LICENSE) file for details.

## Love it!

Via <a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=5ZG69XAD2JMVS" target="_blank">PayPal</a>. Thanks! (-8
