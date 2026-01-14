#FROM eclipse-temurin:17-jdk AS build
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#RUN apt-get update && apt-get install -y maven
#RUN mvn clean package -DskipTests

#FROM eclipse-temurin:17-jre
#WORKDIR /app
#COPY --from=build /app/target/socialApp-0.0.1-SNAPSHOT.jar app.jar
#RUN mkdir -p logs
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app.jar"]

# Stage 1: Build
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
# Använd Maven Wrapper som finns i ditt projekt
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
# Kopiera jar-filen från build-stadiet
COPY --from=build /app/target/socialApp-0.0.1-SNAPSHOT.jar app.jar

# SKAPA LOGG-MAPPEN (Viktigt för din logback-konfiguration)
RUN mkdir -p logs && chmod 777 logs

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]