FROM oracle/graalvm-ce:20.1.0-java11
COPY target/test-project.jar /app/app.jar
RUN gu install -D python
EXPOSE 8080
EXPOSE 5005
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-XX:NativeMemoryTracking=summary", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app/app.jar"]