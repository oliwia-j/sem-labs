FROM openjdk:latest
COPY ./target/lab-01-0.1.0.3-jar-with-dependencies.jar /tmp
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "lab-01-0.1.0.3-jar-with-dependencies.jar"]