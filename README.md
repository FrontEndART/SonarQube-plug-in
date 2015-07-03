# SourceMeter SonarQube plug-in 7.0

*FrontEndART* **[SourceMeter]** is a command line source code analyzer tool, which can perform **deep static analysis** of the source code of complex software systems written in **C/C++**, **Java**, **Python**, and **RPG**. [FrontEndART] offers also a **free version** of SourceMeter.

*SourceMeter SonarQube plug-in* ("plug-in" in the following) is an extension of [SonarQube], an open-source platform for managing code quality. The plug-in executes SourceMeter from the SonarQube environment and uploads the source code analysis results of SourceMeter into the SonarQube database.

The plug-in is specifically developed for SonarQube users, who would like to boost the built-in capabilities of SonarQube and increase productivity. The plug-in is open-source, and provides all the usual SonarQube code analysis results, extended with many additional metrics and issue detectors provided by the SourceMeter tool. The plug-in supports the C/C++, Java, Python, and RPG languages.

Additionally, the plug-in extends the SonarQube GUI with new features on the SonarQube dashboard and drill-down views, making your SonarQube experience more comfortable and your work with the tool more productive.

***Highlights*** of the added features:

- Precise and detailed C/C++, Java, Python, and RPG source code analysis engines

- Class and method level analyses and deeper drill-down views extending the directory and file-based approach of SonarQube

- More sophisticated syntax-based duplicated code detection engine (Type-2 clones)

- Additional and more precise source code metrics (60+) including metrics related to clones

- Powerful coding issue detection

- Additional information on the dashboard

- New hotspot widgets

- And many more...

The plug-in is compatible with the latest LTS (long term support) version 4.5 of SonarQube, which can be obtained from its [website].
(This version of the plug-in was successfully tested with SonarQube versions 4.4, and 4.5.4.)

For more information about SourceMeter SonarQube plug-in please read the User's Guide.

June 12, 2015

(C) FrontEndART Software Ltd. 2001-2015

[FrontEndART]:https://www.frontendart.com/
[SourceMeter]:https://www.sourcemeter.com/
[SonarQube]:http://www.sonarqube.org/
[website]:http://www.sonarqube.org/downloads


# Build
`make -j1`

