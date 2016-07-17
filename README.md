
## JarChecker

JarChecker version 0.8.2

JarChecker was created by bwfcwalshy with help and input from ArsenArsen and I Al Istannen. JarChecker is a program created to check jar files for malicious content. This project was made for use in the Bukkit Forums to protect server owners and people in need of plugins from malicious content. The program is still very much in development and has much to go before it is done.

## Prerequisites
Java 8

Frenflower compiled (See below)

## Build

To build JarChecker you will need to compile the master branch of this repo.  
Once the program has been compiled into a runnable jar file you need to compile fernflower (GitHub repo: https://github.com/fesh0r/fernflower) and place it next to the JarChecker jar.  
Once you have JarChecker and fernflower compiled and in the same location look at the usage section.

## Usage
To use double click the jar to get the GUI or run 
  ``java -jar JarChecker.jar <path | nogui>``
  
Path should be put between "" if it has spaces.
If you typed nogui input the path into the program.
So, for example: 

``java -jar JarChecker.jar C:\Users\You\Desktop\JARs\MyJar.jar``

or

``java -jar JarChecker.jar "C:\Users\You\Desktop\My JARs\MyJar.jar"``

## Contributing
While contributing please use [this formatter](https://github.com/bwfcwalshyPluginDev/JarChecker/files/367925/javaformatter.zip) so we can have commits not too gigantic. It is also needed in order to stop breaking enum structures.
While contributing please put in comments explaining what each line does. That way you make it easier for us and others to potentially optimize it later.
