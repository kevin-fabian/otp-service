# OTP Service

A RESTful service for generating and managing One-Time Passwords (OTP) built with Spring Boot. This service provides
secure OTP generation and validation with support for multiple delivery methods like SMS and Email.

The service also supports Time-based OTP (TOTP) which is compatible with Google Authenticator.
The service is secured with JWT token-based authentication and role-based authorization.

## Features

- Generate OTP codes with configurable length and expiration
- Time-based OTP tested with Google Authenticator
- Multiple delivery methods (SMS, Email) support and can be extended to support other channels
- Stateless Secure REST APIs
- JWT token-based and role-based authentication
- Unit and Integration tests
- Docker support
- Gitflow branching model
- OpenAPI 3.0 documentation
- Swagger UI
- Liquibase for database migrations
- Postgres/H2 database support
- JPA/Hibernate ORM
- Automated Intrumentation via OpenTelemetry
- Http Request Logging via Zalando Logbook

## Tech Stack

- Java 21
- Spring Boot 3.5.5
- Spring Oauth2 Resource Server
- JPA/Hibernate
- PostgreSQL/H2
- Swagger/OpenAPI 3.0
- Zalando Logbook for logging HTTP requests
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

## Implementing `OtpClient` for other delivery methods

```java
public class SMSOtpClient implements OtpClient {

    @Override
    public void send(Otp otpTransaction) {
        // Your implementation here        
    }
}

public class WhatsAppOtpClient implements OtpClient {

    @Override
    public void send(Otp otpTransaction) {
        // implementation        
    }
}
```

## The OtpClient implementation configuration
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
                        DeliveryMethod.SMS, new WhatsAppOtpClient()
                ),
                new DefaultOtpGenerator(),
                otpProperties);
    }
}
```

## API Documentation
API documentation is available via Swagger UI:

- Local: http://localhost:8079/swagger-ui.html
- API Docs: http://localhost:8079/v3/api-docs


## Configuration
The following properties can be configured:

```yml
# OTP configuration
otp:
#  OTP validity period in minutes
  expiration-minutes: 1
#  OTP max attempts
  max-attempts: 3
#  Length of generated OTP codes
  code-length: 6

#Time-based OTP configuration
totp:
  algorithm: SHA1
  digits: 6
  period-seconds: 30
  issuer: App Label
```
