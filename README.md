SonarQube IntelliJ Community Plugin
===================================

The main goal of this plugin is to show [SonarQube](http://sonarqube.org) issues directly from within the IntelliJ IDE.
This plugin is build to work in IntelliJ IDEA, RubyMine, WebStorm, PhpStorm, PyCharm, AppCode or Android Studio and for any programming language you use in these IDE that SonarQube can analyze.
Each sonar issue and rule is converted to an IntelliJ inspection which makes it possible to use inspection features like for any other IntelliJ inspection.

If you have any issues using the plugin, please let us know by [filing a new issue](https://github.com/sonar-intellij-plugin/sonar-intellij-plugin/issues/new), contacting us via the [Google Groups mailing list](https://groups.google.com/forum/#!forum/sonarcube-intellij-plugin) or even sending a pull request. Thanks for your support.


Usage
--------------------

At first you need to configure your sonar server connection. You can use a remote server or a local one on your machine, depends on how you work with sonar.
 
Configuration:

Go to `File -> Settings (Ctrl+Alt+S)-> SonarQube`. 
![alt text][projectConfiguration]
Specify a sonar server `Add -> Name + Host url + Credentials(optional)`
Select the sonar resource `+ -> Download resources`

Local analysis:

This is the tricky part of the plugin. The plugin starts an external script for local analysis. 
To configure your project in the right way three things are important to know.
The working directory: the root directory of the script execution
The source code of the script: the script which executes the sonar-runner, maven, gradle, ant or any other tool which runs sonar-runner behind the scences.
The path to the sonar-report.json: sonar-runner produces an report in json format, which contains issues and new issues in your project. The plugin needs to know the location of this file. It reads the content and shows in the IDE.

Example:
name: java
working dir: <PROJECT>
source code: mvn sonar:sonar -DskipTests=true -Dsonar.language=java -Dsonar.analysis.mode=incremental  -Dsonar.host.url=$SONAR_HOST_URL
path to sonar-report.json: $WORKING_DIR/target/sonar/sonar-report.json

The plugin will do:
go to the <PROJECT> dir: cd /your/project/
execute mvn sonar:sonar -DskipTests=true -Dsonar.language=java -Dsonar.analysis.mode=incremental  -Dsonar.host.url=http://your.url
read sonar issues from: /your/project/target/sonar/sonar-report.json

The plugin replaces special template variables in the source code and the path to sonar-report.json.

$WORKING_DIR: the root directory of script execution, e.g. /my/workingdir
$WORKING_DIR_NAME : the name of the root directory e.g. project

$MODULE_NAME: the IntelliJ module name, e.g. "my module"
$MODULE_BASE_DIR: the directory of the module file, e.g. /your/project/module
$MODULE_BASE_DIR_NAME: the name of the module directory, e.g. module

$PROJECT_NAME: the IntelliJ project name, e.g. "my project"
$PROJECT_BASE_DIR: the root directory of the project, e.g. /your/project
$PROJECT_BASE_DIR_NAME: the name of the project directory, e.g. project

$SONAR_HOST_URL: the sonar host url specified by the sonar server configuration, e.g. http://localhost:9000
$SONAR_SERVER_NAME: the sonar server name specified by the configuraion, e.g. "my sonar"

You can also find this list inside the IDE.

The module configuration is analog, with the only difference that you can use the special setting <PROJECT>.

[projectConfiguration]: http://plugins.jetbrains.com/files/7238/screenshot_14229.png "Example project configuration"


If your project has multiple modules, then you can configure each module as well:

Go to File -> Project Structure (Ctrl+Alt+Shift+S)
-> Modules -> SonarQube Tab
![alt text][moduleConfiguration]

[moduleConfiguration]: http://plugins.jetbrains.com/files/7238/screenshot_14228.png "Example module configuration"

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
