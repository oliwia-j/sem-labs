FROM openjdk:latest
COPY ./target/sem-labs-0.1.0.3-jar-with-dependencies.jar /tmp
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "sem-labs-0.1.0.3-jar-with-dependencies.jar"]