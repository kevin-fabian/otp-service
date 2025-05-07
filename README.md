# OTP Service

A RESTful service for generating and managing One-Time Passwords (OTP) built with Spring Boot. This service provides
secure OTP generation and validation with support for multiple delivery methods like SMS and Email.

This service is meant to be used by upstream service, this is why the security isn't configured here.
If you are using a zero-trust model, then you may configure SSL or integrate Spring Oauth2 resource service.

## Features

- Generate OTP codes with configurable length and expiration
- Multiple delivery methods (SMS, Email) support
- OTP validation and status tracking
- Configurable OTP purposes (Login, etc.)
- Attempt counting and metadata support

## Tech Stack

- Java 21
- Spring Boot 3.x
- JPA/Hibernate
- PostgreSQL
- Swagger/OpenAPI 3.0
- JUnit 5
- Mockito
- Lombok
- Maven
- Liquibase

## Setup & Usage

1. Clone the repository
2. Configure application.properties with your database settings
3. Run `mvn clean install` to build
4. Start the application with `mvn spring-boot:run`

## Implementing `OtpClient`
In a scenario where you need to support another notification service such as SMS, PushNotification, Viber, etc
Implement `OtpClient` and register the new at `AppConfig.defaultOtpService`

## API Documentation

API documentation is available via Swagger UI:

- Local: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs

## Available Endpoints

### OTP Management

- `POST /api/v1/otp` - Generate new OTP
- `POST /api/v1/otp/validation` - Validate OTP(Not yet implemented)

## Configuration

The following properties can be configured:

- `otp.code-length`: Length of generated OTP codes
- `otp.expiration-minutes`: OTP validity period in minutes
- `otp.max-attempts`: OTP max attempts

## Jasypt
```bash
#Run the encryption command:
mvn jasypt:encrypt -Djasypt.encryptor.password="" -Djasypt.plugin.path="file:src/main/resources/application-local.yaml" -Djasypt.encryptor.algorithm="PBEWithHmacSHA256AndAES_256"

#To decrypt a file with encrypted properties:
mvn jasypt:decrypt -Djasypt.encryptor.password="" -Djasypt.plugin.path="file:src/main/resources/application-local.yaml" -Djasypt.encryptor.algorithm="PBEWithHmacSHA256AndAES_256"
```
