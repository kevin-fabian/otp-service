package com.fabiankevin.app.web.dtos;

public record VerifyOtpRequest(String code, String userReferenceId) {
}
