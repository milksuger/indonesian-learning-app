# 后端服务 Docker 镜像
# 基于 Eclipse Temurin JDK 17 Alpine 构建
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
