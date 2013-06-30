Sonar Connector
=====================

The main goal of this plugin is to show sonar violations directly in the IntelliJ IDE. 
This plugin should work in any IDE of the IntelliJ family and for any programming language.
Each sonar violation and rule is converted to an IntelliJ inspection 
and makes it possible to use inspection features like for any other IntelliJ inspection.

Usage
--------------------

First you need to configure your connection to a sonar server. You can connect use a remote server or a local one on your machine.
To configure a project:

Go to File -> Settings (Ctrl+Alt+S)-> Sonar Connector
and test your configuration. 
![alt text][projectConfiguration]

[projectConfiguration]: http://plugins.jetbrains.com/files/7238/screenshot_14229.png "Example project configuration"

If your project has multiple modules, then you can configure each module as well:
Go to File -> Project Structure (Ctrl+Alt+Shift+S)
-> Modules -> Sonar Connector Tab
![alt text][moduleConfiguration]

[moduleConfiguration]: http://plugins.jetbrains.com/files/7238/screenshot_14228.png "Example module configuration"
