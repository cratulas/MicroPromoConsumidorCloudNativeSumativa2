# Etapa de construcción
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/Wallet_BDFullStack3 /app/wallet

EXPOSE 8086

ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=docker $JAVA_OPTS -jar app.jar"]