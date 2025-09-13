# ======================
# Etapa 1: Compilación
# ======================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos solo el pom.xml para descargar dependencias
COPY pom.xml .

# Descargar dependencias offline (acelerar build)
RUN mvn dependency:go-offline -B

# Copiamos el código fuente
COPY src ./src

# Compilamos el proyecto y generamos el JAR con nombre fijo 'app.jar'
RUN mvn clean package -DskipTests

# ======================
# Etapa 2: Runtime
# ======================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiamos el JAR compilado desde la etapa de build
COPY --from=build /app/target/app.jar ./app.jar

# Exponemos el puerto dinámico asignado por Render
ENV PORT=8080
EXPOSE $PORT

# Ejecutamos el JAR usando la variable de entorno PORT
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
