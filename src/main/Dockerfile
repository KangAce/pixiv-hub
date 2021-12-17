FROM openjdk:11
ADD ./docker/*.jar pixivhub.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/pixivhub.jar"]