FROM gradle:7.3.0-jdk17-alpine

ENV JAVA_ARGS='--args="--port=8082"'
ARG PORT='8082'
VOLUME /tmp
COPY src src
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle

#ENTRYPOINT ["gradle","run", "--args=\"--port=8081\""]
ENTRYPOINT ["gradle","run", "--args=\"--port=8081\""]
EXPOSE "8081"