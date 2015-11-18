# DS Workbench

DS Workbench is a planning tool for the browser game "Tribal Wars" (http://www.die-staemme.de/). 
It is implemented as desktop application and written mostly in Java. Formerly, this project and
its releases were hosted at http://www.dsworkbench.de but as of December 24, 2015 this domain
won't be associated with DS Workbench any longer. 

Instead, this code was made available as open source under the Apache License, Version 2.0 and 
everybody who is willing to continue this work is allowed to contribute. However, there are some 
things to be aware of:

* Some PHP-based services, e.g. the luck bar or troop table generator for reports, are still hosted by myself at [http://www.torridity.de](http://www.torridity.de) If you need access to their source code please contact me.
* NEVER integrate any additional interaction with the game itself, e.g no login or reading/sending data directly from/to the game.
* NEVER change or remove the 'Klick-Konto'/'Click-Account' feature from the tool.
* Please do not republish the tool with another name. If you anyhow do, please refer to the original tool 'DS Workbench'.

The final 'official' release (DS Workbench 3.4) will be available at <tbd>

## How to build

In order to build DS Workbench you'll need:

* Java SE Development Kit 7 or newer
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

Afterwards, you'll find DS Workbench in the sub-folder 'release/DSWorkbench-<VERSION>' with a <VERSION> defined in the pom.xml 
The release contains all libraries, config files and start scripts for Unix and Windows. The Unix script has to be made executable
before using it by calling:

```
user@localhost:/home/user/DSWorkbench$ chmod +x release/DSWorkbench-<VERSION>/bin/DSWorkbench
user@localhost:/home/user/DSWorkbench$
```

## More Information

* [Die Staemme Forum Thread](https://forum.die-staemme.de/showthread.php?80831-DS-Workbench)

## License

DS Workbench is licensed under the Apache License, Version 2.0.
