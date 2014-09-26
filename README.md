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

#### Local analysis configuration

After the configuration of the sonar server you are ready to start downloading issues and showing them in the IDE. But as soon you start editing your source code, you might want to trigger a local sonar analysis. To achieve this by using the plugin and showing new issues directly inside the IDE you need to tell the plugin how to analyse your project and provide the path to the sonar-report.json file. The plugin understands the contet of that report file and shows the result in the IDE.
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
your_custom_script_to_perform_local_analysis.sh
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

**NOTE: We will simplify the configuration in a future version.**

<sup>1</sup> _You can only use the plugin for projects which are already present in SonarQube. If your project is not in SonarQube, yet, you'll need to run the analysis first e.g. via sonarrunner, Maven or Gradle._


#### Module Configuration

If your project has multiple modules, you can configure each module individually if you like:

Go to `File -> Project Structure -> Modules -> SonarQube` Tab
![alt text][moduleConfiguration]

[moduleConfiguration]: http://plugins.jetbrains.com/files/7238/screenshot_14228.png "Example module configuration"

#### Syncing

After your project is configured, go to any source file in your project, right click over the source code and press *Sync with sonar*
![alt text][syncWithSonar]
[syncWithSonar]: https://github.com/sonar-intellij-plugin/sonar-intellij-plugin/blob/master/screenshots/sync_with_sonar.jpg?raw=true

If sync is complete (this should take some time depending on your project size and connection), then you can start inspecting your code.
Before I suggest you to create an inspection profile for all those sonar inspections to separate them from live IntelliJ inspections. If you want this:

Go to Analyze -> In the window at bottom right to Inspection Profile select box click onto "..." button.
![alt text][specifyInspectionScope]
[specifyInspectionScope]: https://github.com/sonar-intellij-plugin/sonar-intellij-plugin/blob/master/screenshots/specify_inspection_scope.jpg?raw=true

In the Inspections window click at the top onto the "Add" button and create a new profile, e.g.: Sonar.
Now deselect everything but not the sonar rules for this profile and click "OK"
![alt text][sonarProfile]
[sonarProfile]: https://github.com/sonar-intellij-plugin/sonar-intellij-plugin/blob/master/screenshots/sonar_profile.jpg?raw=true

To inspect your code:

Go to Analyze -> Inspect Code ... -> Choose just created *Sonar* Inspection Profile and Inspection Scope -> OK
You should now see something like:
![alt text][sonarInspectionResult]
[sonarInspectionResult]: https://github.com/sonar-intellij-plugin/sonar-intellij-plugin/blob/master/screenshots/sonar_inspection_result.jpg?raw=true

Please note: you must NOT create a separate inspection profile, you can mix sonar inspection like you want.
One more tip: to quick run only one inspection, press Ctrl+Alt+Shift+I. Type "Sonar" to show all available sonar rules or the name of a rule.

Have fun!

PHPSTORM
------------------------
There is no concept of source directories in PHPSTORM. This means your source root is equal to content root of all project files.
The consequences are you must configure your sonar source to content route directory. To do so make sure your sonar.properties is located in content root directory and edit:
```
sonar.sources=.
```
Run sonar-runner (this is out of the scope of this plugin) and sync with sonar in PHPSTORM. Now you should be able to see php inspections in phpstorm in the same way as in IntelliJ IDEA.
