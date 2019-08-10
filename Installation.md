# FASTDial Installation, Compile and Run

## To compile and run FASTDial, please follow the instructions below:

1- Download OpenDial toolkit http://www.opendial-toolkit.net/download
2- Copy opendial-1.4.jar into the lib folder
3- Download telegrambots packages ( https://github.com/rubenlagus/TelegramBots ) and move them to
   the lib folder. The required packages are 
   telegrambots-3.6-jar-with-dependencies.jar
   telegrambots-abilities-3.6-jar-with-dependencies.jar 
   telegrambots-meta-3.6-jar-with-dependencies.jar
   telegrambotsextensions-3.6-jar-with-dependencies.jar
4- If you do not want to install gradle, please download necessary dependencies specified in the build.gradle file into the lib folder.
5- Otherwise, if you do not have gradle, please install gradle.

## Telegram Installation Guide

1- Create a telegram bot and obtain a BotUsername and BotToken.
2- Fill telegramBotUsername and telegramBotToken in telegram.properties with the obtained username and token.
3- Build FastDial using build.gradle.
4- Run the telegram bot using gradle application: gradle run
5- If you do not want to use gradle: run /FASTDial/src/fastdial/interfaces/telegram/RegisterBot.java as java application


## Service Installation Guide

1- Install a Tomcat server that is version 6 or higher.
2- If you are using Eclipse IDE, run the FASTDial project on server. 
3- If you want to run the .WAR package only:

Running .war on an external Servlet:

1. Move fastdial.properties file into the webapps folder of Tomcat. The folder should be found in `{catalina_base}/webapps`
2. Create a log folder for the FASTDial Service, e.g. `mkdir {catalina_base}/logFolder`
3. Open `{catalina_base}/webapps/fastdial.properties` and set the logPath as the "Absolute" path of the new log folder.
4. The other paths can be kept as they are if any change has not been done to the WAR contents.
5. Copy FASTDial.war into `{catalina_base}/webapps/`

A dialogue can be started through the link below:

service_address:tomcat_port/FASTDial/rest/bot
