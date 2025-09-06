# ======================
# Etapa 1: Compilación
# ======================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos primero el pom.xml y descargamos dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente
COPY src ./src

# Compilamos el proyecto y generamos el jar con nombre fijo
RUN mvn clean package -DskipTests

# ======================
# Etapa 2: Runtime
# ======================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiamos el jar compilado
COPY --from=build /app/target/app.jar app.jar

# Render asigna el puerto en $PORT (no hardcodear)
EXPOSE 8080

# Comando de ejecución
CMD ["java", "-jar", "app.jar"]
