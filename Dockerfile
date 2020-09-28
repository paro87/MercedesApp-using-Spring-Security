FROM openjdk:12.0.1-jdk
MAINTAINER Palvan Rozyyev <pelvan657@yahoo.com>
COPY /target/mercedesApp.jar mercedesApp.jar
ENTRYPOINT ["java", "-jar", "mercedesApp.jar"]
EXPOSE 9090