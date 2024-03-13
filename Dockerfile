FROM openjdk:latest
COPY ./target/SEMlabs.jar /tmp
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "SEMlabs.jar", "db:3306", "30000"]