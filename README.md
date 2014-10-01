SonarQube IntelliJ Community Plugin
===================================

The main goal of this plugin is to show [SonarQube](http://sonarqube.org) issues directly from within the IntelliJ IDE.
This plugin is build to work in IntelliJ IDEA, RubyMine, WebStorm, PhpStorm, PyCharm, AppCode or Android Studio and for any programming language you use in these IDE that SonarQube can analyze.
Two tasks are covered by the plugin: 
* downloading already analyzed code from sonar server and show them in the IDEA
* running a local analysis script and analyze new issues

If you have any issues using the plugin, please let us know by [filing a new issue](https://github.com/sonar-intellij-plugin/sonar-intellij-plugin/issues/new), contacting us via the [Google Groups mailing list](https://groups.google.com/forum/#!forum/sonarqube-intellij-plugin) or even sending a pull request. Thanks for your support.


### Usage

#### Project Configuration

After the installation you first of all need to configure the connection to your sonar server. This is done per project and/or module. You can use a remote server or a local one on your machine, depends on how you work with sonar.

Go to `File -> Settings (Ctrl+Alt+S)-> SonarQube`. 

![alt text][serverSelection]
[serverSelection]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/sonar_4/screenshots/server_selection.png "Example server selection"

Click Add and configure sonar server

![alt text][serverConfiguration]
[serverConfiguration]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/sonar_4/screenshots/server_configuration.png "Example server configuration"

Select the sonar resource

![alt text][resourceSelection]
[resourceSelection]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/sonar_4/screenshots/resource_selection.png "Example resource selection"

The finished sonar server configuration should look like:

![alt text][serverConfigurationComplete]
[serverConfigurationComplete]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/sonar_4/screenshots/server_configuration_complete.png "Example resource selection"

#### Code inspection

The plugin provides two inspections:
* SonarQube - shows already analysed issues
* SonarQube (new issues) - shows only new issues from local analysis

To perform a code inspection you can:
Go to `Analyze -> Inspect code`.
Select whole project. It is recommended to create a Sonar Inspection profile, with sonar inspections only, but you can also use default profile or any other self defined inspection profile.

After the execution the inspection result should look like:
![alt text][analysisResults]
[analysisResults]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/sonar_4/screenshots/analysis_results.png "Example resource selection"

As the sonar analysis process is prone to errors, it is essential to see what happened during the analysis. You can use the sonar console for error analysis, especially during the first time configuration:
![alt text][sonarConsole]
[sonarConsole]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/sonar_4/screenshots/sonar_console2.png "Example resource selection"

#### Local analysis configuration

After the configuration of the sonar server you are ready to start downloading issues and showing them in the IDEA. But as soon you start editing your source code, you might want to trigger a local sonar analysis. To achieve this by using the plugin and showing new issues directly inside the IDEA, you need to tell the plugin how to analyse your project and provide the path to the sonar-report.json file. The plugin understands the contents of the report file and shows the results in the IDEA like any other inspection.
Before configuring the plugin, you need to understand how to run local analysis for your project. 

Go to your prefered console and try to run depending on your project something like:

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

This tells you where to find the sonar-report.json. This file is essential, as it tells the plugin the location of the new issues.

**NOTE: The configuration of the local analysis is out of the scope of the plugin, please read the SonarQube documation about how to perform it**

After you know how to perform the local analysis, you need to configure the plugin:

Go to `File -> Settings (Ctrl+Alt+S)-> SonarQube`.

![alt text][localScriptManagement]
[localScriptManagement]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/sonar_4/screenshots/local_script_management.png "Example local script management"

Click add and define:

* an unique name, e.g. java-only
* the script itself, e.g. sonar-runner -Dsonar.analysis.mode=incremental ...
* path to the sonar-report.json

A finished configuration can look like:
![alt text][localScriptConfigured]
[localScriptConfigured]: https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/sonar_4/screenshots/local_script_configured.png "Example local script management"

**NOTE: If command like "mvn" or "sonar-runner" does not work, you can use a full path instead: /path/to/mvn.**

##### Placeholders

In the previous example we have used a hard coded script and a sonar-report.json file path using a $WORKING_DIR placeholder. You can use several placeholders to replace values in your script or sonare-report.json file path.

placeholder | meaning
----------- | -------
$WORKING_DIR | the working directory of script executin, e.g. /path/to/project
$WORKING_DIR_NAME | the name of the working directory without the full path, e.g. project
$MODULE_NAME | the name of the module, e.g. example-java-maven
$MODULE_BASE_DIR | the directory of the .iml file, e.g. /path/to/project/module
$MODULE_BASE_DIR_NAME | the name of the directory of the .iml file, e.g. module
$PROJECT_NAME | the name of the project, e.g. my project
$PROJECT_BASE_DIR | the project root directory, e.g. /path/to/project
$PROJECT_BASE_DIR_NAME | the name of the project root directory, e.g. project
$SONAR_HOST_URL | the sonar host url, e.g. http://localhost:9000
$SONAR_SERVER_NAME | the sonar server name, e.g. my server
$SONAR_USER_NAME | the sonar user name, e.g. my_user
$SONAR_USER_PASSWORD | the sonar user password, e.g. pw

Using the placeholders you can define one script and reuse it in several project. It is also usefull if your project is a multi module project.
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

**NOTE: how the $WORKING_DIR is replaced by /path/to/project for project and by /path/to/project/module for a module.**

**NOTE: if your module.iml files are not located in same directory as the module root, then you can override the working directory manually.**

#### Module configuration
The module configuration is similar the project configuration. Please note that for a multi module maven project you need to manually define the sonar resource for each module. 

Go to `File -> Project Structure (Ctrl+Alt+Shift+S)-> Select a module -> SonarQube Tab`.
Configure the module in same way like a project. You can use a special option `<PROJECT>`, in this case the project configuration will be used.
The local analysis script is per default starting in the module base directory.

Example:
multi mvn project

```
project
  module1
    pom.xml 
  module2
    pom.xml
  pom.xml <- parent
```

if analysing module1 a script will download the issues configured by module configuration and start a local analysis script in `project/module2/`.
if analysing whole project plugin will download sonar resource configured in project settings and start a local analysis script in `project/`
You can use same local script configuration for module or project level analysis:

script
up_to_you.sh $SONAR_HOST_URL

path so sonar-report.json
$WORKING_DIR/target/sonar/sonar-report.json

possible contents of the up_to_you.sh script:
```
#!/bin/bash
export JAVA_HOME="/path/to/jdk1.8.0.jdk/Home/"
export MAVEN_OPTS="-XX:MaxPermSize=128m"
/path/to/mvn sonar:sonar -DskipTests=true -Djava.awt.headless=true -Dsonar.language=java -Dsonar.analysis.mode=incremental -Dsonar.host.url=$1 -Dsonar.profile=mobile_relaxed_java_8```
