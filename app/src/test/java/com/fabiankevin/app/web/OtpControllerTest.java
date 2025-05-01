package com.fabiankevin.app.web;

import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.services.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OtpController.class)
class OtpControllerTest {
    private static final DateTimeFormatter DEFAULT_CUSTOM_ISO8601_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OtpService otpService;

    private Otp mockedOtp;

    @BeforeEach
    public void setup() {
        OffsetDateTime now = OffsetDateTime.now();
        mockedOtp = Otp.builder()
                .id(UUID.randomUUID())
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .userIdentifier("test@test.com")
                .status(OtpStatus.ACTIVE)
                .metadata("test metadata")
                .attemptCount(0)
                .createdAt(now)
                .expiresAt(now.plusMinutes(1))
                .otpCode("123456")
                .build();
    }

    @Test
    void generateOtp_givenValidRequest_thenShouldReturnOtpCode() throws Exception {
        when(otpService.generate(any()))
                .thenReturn(mockedOtp);

        mockMvc.perform(post("/api/v1/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "user_identifier": "test@test.com",
                                   "purpose": "LOGIN",
                                   "delivery_method": "EMAIL",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockedOtp.id().toString()))
                .andExpect(jsonPath("$.otp_code").value("123456"))
                .andExpect(jsonPath("$.expired_at").value(mockedOtp.expiresAt().format(DEFAULT_CUSTOM_ISO8601_FORMAT)));

        verify(otpService, times(1)).generate(any());
    }


    @Test
    void generateOtp_givenInvalidPurpose_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "user_identifier": "test@test.com",
                                   "purpose": "INVALID_PURPOSE",
                                   "delivery_method": "EMAIL",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details").value("""
                        Invalid request data.
                        OtpPurpose must be one of the following: LOGIN, RESET_PASSWORD, TRANSACTION, VERIFICATION
                        DeliveryMethod must be one of the following: SMS, EMAIL, PUSH
                        """));

        verify(otpService, never()).generate(any());
    }

    @Test
    void generateOtp_givenInvalidDeliveryMethod_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "user_identifier": "test@test.com",
                                   "purpose": "LOGIN",
                                   "delivery_method": "INVALID_METHOD",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details").value("""
                        Invalid request data.
                        OtpPurpose must be one of the following: LOGIN, RESET_PASSWORD, TRANSACTION, VERIFICATION
                        DeliveryMethod must be one of the following: SMS, EMAIL, PUSH
                        """));

        verify(otpService, never()).generate(any());
    }

    @Test
    void generateOtp_givenInvalidUserIdentifier_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "user_identifier": "",
                                   "purpose": "LOGIN",
                                   "delivery_method": "EMAIL",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(otpService, never()).generate(any());
    }

}
