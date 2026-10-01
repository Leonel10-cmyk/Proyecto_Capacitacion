# ========================================================
# Etapa 1: Build de la aplicación con Maven y OpenJDK 11
# ========================================================
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app

# Copiar pom y descargar dependencias (para aprovechar cache de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# ========================================================
# Etapa 2: Imagen ligera de ejecución con JRE 11
# ========================================================
FROM eclipse-temurin:11-jre
WORKDIR /app

# Crear directorio para subida de comprobantes
RUN mkdir -p uploads/comprobantes

# Copiar el jar compilado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Render asigna dinámicamente la variable PORT
ENV PORT=8081
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
