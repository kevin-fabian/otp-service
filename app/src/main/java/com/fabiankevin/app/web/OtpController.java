package com.fabiankevin.app.web;

import com.fabiankevin.app.services.OtpService;
import com.fabiankevin.app.web.dtos.OtpRequest;
import com.fabiankevin.app.web.dtos.OtpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PostMapping
    public OtpResponse generate(OtpRequest otpRequest) {
        return OtpResponse.from( otpService.generate(otpRequest.toCommand()));
    }
}
