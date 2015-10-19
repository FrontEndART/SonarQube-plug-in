# FrontEndART SourceMeter plug-in for SONARQUBE™ platform

*FrontEndART* **[SourceMeter]** is a command line source code analyzer tool, which can perform **deep static analysis** of the source code of complex software systems written in **C/C++**, **Java**, **Python**, and **RPG**. [FrontEndART] offers also a **free version** of SourceMeter.

*SourceMeter plug-in for SONARQUBE™ platform* ("plug-in" in the following) is an extension of the open-source [SONARQUBE]™ platform for managing code quality. The plug-in executes SourceMeter from the SONARQUBE™ platform and uploads the source code analysis results of SourceMeter into the SONARQUBE™ database.

The plug-in is open-source, and provides all the usual SONARQUBE™ code analysis results, extended with many additional metrics and issue detectors provided by the SourceMeter tool. The plug-in supports the C/C++, Java, Python and RPG languages.

Additionally, the plug-in extends the SONARQUBE™ platform GUI with new features on the dashboard and drill-down views.

[FrontEndART]:https://www.frontendart.com/
[SourceMeter]:https://www.sourcemeter.com/
[SONARQUBE]:http://www.sonarqube.org/

***Highlights*** of the added features:

- Precise and detailed C/C++, Java, Python, and RPG source code analysis engines

- Class and method level analyses and deeper drill-down views extending the directory and file-based approach

- More sophisticated syntax-based duplicated code detection engine (Type-2 clones)

- Additional and more precise source code metrics (60+) including metrics related to clones

- Powerful coding issue detection

- Additional information on the dashboard

- New hotspot widgets

- And many more...

The plug-in is compatible with the latest LTS (long term support) version 4.5[^1] of the SONARQUBE™ platform, which can be obtained from its [website]. (This version of the plug-in was successfully tested with versions 4.4, and 4.5.4.)

For more information about the plug-in please read the User's Guide.

SONARQUBE™ is a trademark of SonarSource SA, Switzerland.

October 19, 2015

(C) FrontEndART Software Ltd. 2001-2015


[FrontEndART]:https://www.frontendart.com/
[SourceMeter]:https://www.sourcemeter.com/
[SonarQube]:http://www.sonarqube.org/
[website]:http://www.sonarqube.org/downloads


# Build
`make -j1`
