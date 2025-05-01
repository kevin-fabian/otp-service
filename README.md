# OTP Service

A RESTful service for generating and managing One-Time Passwords (OTP) built with Spring Boot. This service provides
secure OTP generation and validation with support for multiple delivery methods like SMS and Email.

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

## Setup & Usage

1. Clone the repository
2. Configure application.properties with your database settings
3. Run `mvn clean install` to build
4. Start the application with `mvn spring-boot:run`

## API Documentation

API documentation is available via Swagger UI:

- Local: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## Available Endpoints

### OTP Management

- `POST /api/v1/otp/generate` - Generate new OTP
- `POST /api/v1/otp/validate` - Validate OTP
- `GET /api/v1/otp/{id}` - Retrieve OTP by ID

## Configuration

The following properties can be configured:

- `otp.code-length`: Length of generated OTP codes
- `otp.expiration-minutes`: OTP validity period in minutes

