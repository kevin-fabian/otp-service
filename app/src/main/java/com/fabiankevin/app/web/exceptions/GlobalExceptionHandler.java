package com.fabiankevin.app.web.exceptions;

import com.fabiankevin.app.exceptions.ApiException;
import com.fabiankevin.app.web.dtos.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(ApiException ex) {
        return ResponseEntity.status(HttpStatusCode.valueOf(ex.getHttpStatusCode()))
                .body(new ApiErrorResponse("Resource Error", ex.getMessage()));

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        return new ApiErrorResponse("Validation Failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return new ApiErrorResponse("Validation Failed", """
                Invalid request data.
                OtpPurpose must be one of the following: LOGIN, RESET_PASSWORD, TRANSACTION, VERIFICATION
                DeliveryMethod must be one of the following: SMS, EMAIL, PUSH
                """);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGenericException(Exception ex) {
        return new ApiErrorResponse("Internal Server Error", "something went wrong");
    }
}
