package com.fabiankevin.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fabiankevin.microwebspringbootstarter.web.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class InvalidJwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private static final String DEFAULT_UNAUTHORIZED_MESSAGE = "Unauthorized";
    private static final String DEFAULT_UNAUTHORIZED_DETAILS = "Invalid or expired token";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ApiErrorResponse errorResponse = new ApiErrorResponse();
        errorResponse.setMessage(DEFAULT_UNAUTHORIZED_MESSAGE);
        errorResponse.setDetails(DEFAULT_UNAUTHORIZED_DETAILS);
        errorResponse.setTimestamp(Instant.now());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
