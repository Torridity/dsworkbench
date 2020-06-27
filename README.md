# DS Workbench

[![Github Releases (by Release)](https://img.shields.io/github/downloads/Torridity/dsworkbench/3.72/total.svg)](https://github.com/Torridity/dsworkbench/releases/tag/3.72)

DS Workbench is a planning tool for the browser game "Tribal Wars" (http://www.die-staemme.de/).
It is implemented as desktop application and written mostly in Java. Formerly, this project and
its releases were hosted at http://www.dsworkbench.de but as of December 24, 2015 this domain
won't be associated with DS Workbench any longer.

Instead, this code was made available as open source under the Apache License, Version 2.0 and
everybody who is willing to continue this work is allowed to contribute. However, there are some
things to be aware of:

* Some PHP-based services, e.g. the luck bar or troop table generator for reports, are still hosted
by myself at [http://www.torridity.de](http://www.torridity.de). If you need access to their source
code please contact me.
* NEVER integrate any additional interaction with the game itself, e.g no login or reading/sending
data directly from/to the game.
* NEVER change or remove the 'Klick-Konto'/'Click-Account' feature from the tool.
* Please do not republish the tool with another name. If you anyhow do, please refer to the original
tool 'DS Workbench'.

All *official* releases you can find [here](https://github.com/Torridity/dsworkbench/releases).

## How to install

* Install the latest OpenJDK (JRE versions will be enough for running DSWorkbench) for your platform from [https://adoptopenjdk.net/](https://adoptopenjdk.net/) and follow the installation instructions (you may also use the official releases but setup might need much more effort and expertise)
* Download the current release, e.g. DSWorkbench-3.72.zip and extract the file into a new folder, preferably into your user directory, not containing blanks, e.g. DSWorkbench

### Linux/MacOS

* Open a terminal window and change to the folder where you extracted DS Workbench, by calling e.g. `cd ~/DSWorkbench`
* Make the launch script executable by calling `chmod +x DSWorkbench`
* Run DSWorkbench from the current folder by calling `./DSWorkbench`

### Windows

* Open the file explorer and change into the folder where you extracted DS Workbench
* Press and hold the 'Alt' key and drag the file 'DSWorkbench.bat' to your Desktop, which will create a shortcut (don't forget pressing 'Alt', otherwise the file is moved to the Desktop and DSWorkbench won't run)
* Launch DSWorkbench by double-clicking the shortcut on your Desktop

## How to build

In order to build DS Workbench you'll need:

* OpenJDK 9 or newer
* Apache Maven 3

After obtaining the sources change to the folder where the sources are located and just call:

```
user@localhost:/home/user/DSWorkbench$ mvn install assembly:assembly
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO]
[INFO] DSWorkbench
[INFO] Core
[INFO] ParserPlugin
[...]
user@localhost:/home/user/DSWorkbench$
```

Afterwards, you'll find DS Workbench in the sub-folder `release/DSWorkbench-<VERSION>` with a
`<VERSION>` defined in the pom.xml. The release contains all libraries, config files and start
scripts for Unix and Windows. The Unix script has to be made executable before using it by calling:

```
user@localhost:/home/user/DSWorkbench$ chmod +x release/DSWorkbench-<VERSION>/bin/DSWorkbench
user@localhost:/home/user/DSWorkbench$
```

## More Information

* [Die Staemme Forum Thread](https://forum.die-staemme.de/index.php?threads/ds-workbench.80831/)

## License

DS Workbench is licensed under the Apache License, Version 2.0.
