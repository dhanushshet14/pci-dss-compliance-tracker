# =============================================================================
# Stage 1 — Build
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache the dependency layer separately from the source layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the fat JAR (skip tests — they run in CI)
COPY src ./src
RUN mvn clean package -DskipTests -B

# =============================================================================
# Stage 2 — Runtime
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user / group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the fat JAR produced in the build stage
COPY --from=builder /build/target/*.jar app.jar

# Give ownership of the JAR to the non-root user
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# exec form — no shell wrapper, signals are received directly by the JVM
ENTRYPOINT ["java", "-jar", "app.jar"]
