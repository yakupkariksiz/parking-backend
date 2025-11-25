# -------- Build stage --------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Önce sadece pom.xml, cache daha iyi çalışsın
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Şimdi kaynak kodu kopyalayalım
COPY src ./src

# Jar'ı build et
RUN mvn -q -DskipTests package

# -------- Run stage --------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Jar ismini pom.xml'deki artifactId + version'a göre güncelle
COPY --from=build /app/target/parking-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
