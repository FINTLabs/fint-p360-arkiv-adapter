FROM gradle:4.6.0-jdk8-alpine as builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM openjdk:8-jre-alpine
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/deps/external/*.jar /data/
COPY --from=builder /home/gradle/build/deps/fint/*.jar /data/
COPY --from=builder /home/gradle/build/libs/fint-ra-personal-adapter-*.jar /data/fint-ra-personal-adapter.jar
CMD ["/data/fint-ra-personal-adapter.jar"]
