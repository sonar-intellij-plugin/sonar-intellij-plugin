# Local Analysis

After configuring the Sonar server you are ready to start downloading issues and showing them in the IDEA. But as soon you start editing your source code, you might want to trigger a local sonar analysis. To achieve this by using the plugin and showing new issues directly inside the IDEA, you need to tell the plugin how to analyse your project and provide the path to the sonar-report.json file. The plugin understands the contents of the report file and shows the results in IDEA like any other inspection.
Before configuring the plugin, you need to understand how to run local analysis for your project. 

Go to your preferred console and try to run depending on your project something like:

```
# With maven
mvn sonar:sonar -DskipTests=true -Dsonar.language=java -Dsonar.analysis.mode=preview -Dsonar.report.export.path=sonar-report.json -Dsonar.host.url=$SONAR_HOST_URL
# With gradle
gradle sonarqube -DskipTests=true -Dsonar.language=java -Dsonar.analysis.mode=preview -Dsonar.report.export.path=sonar-report.json -Dsonar.host.url=$SONAR_HOST_URL
```

**NOTE: `sonar.report.export.path` parameter is mandatory to generate analysis result**

**NOTE: `sonar.analysis.mode=preview` parameter will not store result in remote SonarQube database, which is what we want for local analysis**

or

```
your_custom_script_to_perform_local_analysis.sh # may use gradle, ant, what ever else you prefer
```

After the script is done, your should see `BUILD SUCCESSFUL` or something like with maven:

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

**NOTE: The configuration of the local analysis is out of the scope of the plugin, please read the SonarQube documentation about how to perform it**

After you know how to perform local analysis, you need to configure the plugin:

Go to `File -> Settings (Ctrl+Alt+S)-> SonarQube`.

![alt text](https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/local_script_management.png "Example local script management")

Click add and define:

* a unique name, e.g. java-only
* the script itself, e.g. `mvn sonar:sonar -Dsonar.analysis.mode=preview ...` (or `gradle sonarqube ...` for gradle)
* path to the sonar-report.json

A finished configuration can look like:
![alt text](https://raw.github.com/sonar-intellij-plugin/sonar-intellij-plugin/master/screenshots/local_script_configured.png "Example local script management")

**NOTE: If command like "mvn" does not work, you can use a full path instead: /path/to/mvn.**

**NOTE: For Gradle, it is recommended to [use a wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).**

### Placeholders

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
`SONAR_ACCESS_TOKEN` | the sonar access Token, e.g. 3088a92e431a655030fe2244916ddc9ec172e33d

Using the placeholders you can define one script and reuse it in several projects. It is also useful if your project is a multi module project.
For example in a multi module project, you will find `sonar-report.json` in following folders:

- `/path/to/project/target/sonar/sonar-report.json` if executing analysis (see previous maven / gradle command) in project folder (i.e. `/path/to/project`).
- `/path/to/project/mobule/target/sonar/sonar-report.json` if executing analysis in a module folder (i.e. `/path/to/project/module`).

**NOTE: if your module.iml files are not located in same directory as the module root, then you can override the working directory manually.**

## Module configuration

Module configuration is similar project configuration. **Please note that for a multi module maven project you need to manually define the sonar resource for each module.**

Go to `Project Structure -> Select a module -> Select SonarQube Tab`.

Configure the module in the same way as a project. You can use a special option `<PROJECT>`, in this case the project configuration will be used.
The local analysis script is per default, starting in the module base directory.

### Example: A multi-module maven project

With maven:

```
project
  module1
    pom.xml 
  module2
    pom.xml
  pom.xml <- parent
```

With gradle:

```
project
  module1
    build.gradle 
  module2
    build.gradle
  build.gradle <- parent
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
./gradlew sonarqube \
                    -DskipTests=true \
                    -Dsonar.language=java \
                    -Dsonar.analysis.mode=preview \
                    -Dsonar.report.export.path=sonar-report.json \
                    -Dsonar.host.url=${SONARQUBE_HOST_URL} \
                    -Dsonar.profile=your_java_profile
                    --info --stacktrace
```

Tip: Omit the `sonar.language` parameter if you have multiple languages in your project (e.g. Java and Groovy).
