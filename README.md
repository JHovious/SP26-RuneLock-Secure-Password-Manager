# RuneLock - Secure Password Manager  

The purpose of RuneLock is to provide secure password management for families, businesses, and individuals.
RuneLock allows users to manage their own data easily and effectively without fear of their information being shared or sold to third parties. 
By providing users with an offline, encrypted database hosted on their own machines, we aim to give them reassurance that their data is safe and in their own hands.


## Dependencies (Must be installed before running)
To run RuneLock in its current state, ensure you have the following packages with the correct versions installed on your machine. 
If you do not have the correct version or the package is missing, skip to the next section for instructions on installing them for your specific operating system. 
- Apache Ant: 1.10.14 or newer. 
- Java JDK 23 or newer. 
- JavaFX SDK: 21.0.10 or newer


## (WINDOWS) Dependency Installation
- Step 1: Install Apache Ant 
Click [HERE](https://ant.apache.org/manual/install.html) for instructions on installing Ant on Windows.
- Step 2: Download JavaFX
Click [HERE](https://download2.gluonhq.com/openjfx/21.0.11/openjfx-21.0.11_windows-x64_bin-sdk.zip) to download JavaFX. IMPORTANT: Make sure you extract the zip folder
to `C:\javafx-sdk-21`
- Step 3: Install Java 23
Click [HERE](https://download.oracle.com/java/23/archive/jdk-23_windows-x64_bin.exe) to download Java 23. Once the download finishes, double-click the .exe installer and
follow the installation steps provided by the installer.
- Step 4: Set your `JAVA_HOME` and `PATH` Environment Variables
Click [HERE](https://www.geeksforgeeks.org/java/setting-environment-java/) for instructions on setting up your environment variables. IMPORTANT: This is a mandatory step
before the program will run.


## (MAC) Dependency Installation
- Step 1: Install Apache Ant
Open your terminal and enter the command `brew install ant`. Once that finishes, enter the command `ant -version` to verify Ant was installed correctly.
- Step 2: Install Java 23
Click [HERE](https://www.oracle.com/java/technologies/javase/jdk23-archive-downloads.html) to find the Java 23 file options. Scroll to the bottom of the page to find:
"Java SE Development Kit 23". Once you have found this section, download either the "macOS Arm 64 DMG Installer" or "macOS x64 DMG Installer" depending on your chip type.
Once the download finishes, double-click the .exe installer and follow the installation steps provided by the installer.

## (LINUX - Ubuntu/Debian/Mint) Dependency Installation
- Step 1: Install Apache Ant
Open your terminal and enter the command `sudo apt update` and then `sudo apt install ant`. Once that finishes, enter the command `ant -version` to verify Ant was
installed correctly. 
- Step 2: Install Java 23
Click [HERE](https://download.oracle.com/java/23/archive/jdk-23_linux-x64_bin.deb) to download Java 23.
Once the download has completed, open your terminal and navigate to your downloads folder, and enter the command `sudo dpkg -i jdk-23_linux-x64_bin.deb`.
Once that finishes, enter the command `java -version` to verify Java 23 was installed correctly. 


## Getting Started  

In its current state, there is no executable ready for RuneLock. To use this project, copy the files from our most current release folder: 
[release 1.f1](https://github.com/JHovious/SP26-RuneLock-Secure-Password-Manager/tree/main/release1.f1/runelock) Since there is no executable, 
the project must be run from the command line with Ant commands. 

## Installing & Running

1. Copy the files from the most current release found in the "Getting Started" section above.
2. Download the files to a location you can easily remember on your machine.
3. This section will explain the steps for locating and running RuneLock for the first time. The next section will explain important features of RuneLock.
## (WINDOWS) 
 - Open your terminal
 - Navigate to the file location *for example* `cd C:\users\user\Desktop\runelock`. Enter the command `ls` and confirm you see the `build.xml` file.
 - Once you are in the correct location, use the Ant command: `ant run` to build and run the project. 
 - If done correctly, you should see the sign-in page appear.
## (MAC / LINUX)
 - Open your terminal
 - Navigate to the file location *for example* `cd Desktop/runelock`. Enter the command `ls` and confirm you see the `build.xml` file.
 - Once you are in the correct location, use the Ant commands: `ant install-javafx`. Once the install is finished, enter the Ant command: `ant run`

## Built With
 - [Apache Ant](https://ant.apache.org/)
 - [JavaFX](https://openjfx.io/)

## Authors
 - Christian Kurdi
 - Justin Hovious
