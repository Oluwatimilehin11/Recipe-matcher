# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Copy build wrappers and config
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Resolve dependencies in cache
RUN ./mvnw dependency:go-offline -B

# Copy source and package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Run non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]