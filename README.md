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
- Spring Boot 3.5.5
- Spring Oauth2 Resource Server
- JPA/Hibernate
- PostgreSQL/H2
- Swagger/OpenAPI 3.0
- JUnit 5
- Mockito
- Lombok
- Maven
- Liquibase
- Gitflow

## Setup & Usage

1. Clone the repository
2. Configure application.properties with your database settings
3. Run `mvn clean install` to build
4. Start the application with `mvn spring-boot:run`

## Implementing `OtpClient`

```java
public class SMSOtpClient implements OtpClient {

    @Override
    public void send(Otp otpTransaction) {
        // Your implementation here        
    }
}

public class PushNotifcationOtpClient implements OtpClient {

    @Override
    public void send(Otp otpTransaction) {
        // implementation        
    }
}
```
The OtpClient implementation configuration
```java
@Configuration
public class AppConfig {
    @Bean
    public OtpService defaultOtpService(OtpRepository otpTransactionRepository,
                                        EmailOtpClient emailOtpClient,
                                        OtpProperties otpProperties) {
        return new DefaultOtpService(otpTransactionRepository,
                Map.of(
                        DeliveryMethod.EMAIL, emailOtpClient,
                        DeliveryMethod.SMS, new SMSOtpClient(),
                        DeliveryMethod.SMS, new PushNotifcationOtpClient()
                ),
                new DefaultOtpGenerator(),
                otpProperties);
    }
}
```


In a scenario where you need to support another notification service such as SMS, PushNotification, Viber, etc.
Implement `OtpClient` and register the new client at `AppConfig.defaultOtpService`

## API Documentation

API documentation is available via Swagger UI:

- Local: http://localhost:8079/swagger-ui.html
- API Docs: http://localhost:8079/v3/api-docs

## Available Endpoints

### OTP Management

- `POST /v1/otps` - Generate new OTP
- `POST /v1/otps/{otpId}/verify` - Validate OTP

## Configuration

The following properties can be configured:

```yml
otpTransaction:
#  OTP validity period in minutes
  expiration-minutes: 1
#  OTP max attempts
  max-attempts: 3
#  Length of generated OTP codes
  code-length: 6
```
