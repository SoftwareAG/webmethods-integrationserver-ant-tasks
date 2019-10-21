# webmethods-integrationserver-ant-tasks

Ant tasks for automation of webMethods Integration Server

## Prerequisites

- Install [Apache Ant](https://ant.apache.org) version 1.7 or higher
- Configure the following environment variables
  - `ANT_HOME` pointing to your Ant installation location
  - `WEBMETHODS_HOME` pointing to the location directory of the webMethods suite (not Integration Server)
  - `JAVA_HOME` pointing to a JDK. The easiest way will be to leverage the value from `WEBMETHODS_HOME`.
    - Linux/UNIX/macOS:  `JAVA_HOME=$WEBMETHODS_HOME/jvm/jvm` (enclose in double-quotes, if the path contains spaces)
    - Windows: `JAVA_HOME=%WEBMETHODS_HOME%\jvm\jvm`
- Ensure that `$ANT_HOME/bin` and `$JAVA_HOME/bin` are part of the search path (`PATH` variable)

## Installation

- Build jar file by simply calling Ant with the default target: `ant`
- The jar file can be found in `./build` under the name `wm-is-ant-tasks_<VERSION>.jar`


## Usage

- Include jar file in your Ant script as shown on `samples.xml`.
- The connection parameters, since they are ordinary Ant properties (`-Dproperty.name="Property value"`), can also be provided on the command line.

## Example

To reload an Integration Server package:
``` sh
ant -f samples.xml -DwebMethods.package.name=Default reload-package
```

There are many more examples in the `samples.xml` file.



______________________
These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.

Contact us at [TECHcommunity](mailto:technologycommunity@softwareag.com?subject=Github/SoftwareAG) if you have any questions.
