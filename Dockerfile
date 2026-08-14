FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY target/java-remittance-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
