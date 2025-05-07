package com.fabiankevin.app.web;

import com.fabiankevin.app.exceptions.OtpNotFoundException;
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
    void setup() {
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
                .andExpect(jsonPath("$.expired_at").exists());

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

    @Test
    void verifyOtp_givenValidRequest_thenShouldPassVerification() throws Exception {
        doNothing().when(otpService).verify(any());

        mockMvc.perform(post("/api/v1/otp/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "otp_code": "123456",
                                   "reference_id": "c0a80123-4567-890a-bcde-f0123456789a"
                                }
                                """))
                .andExpect(status().isOk());

        verify(otpService, times(1)).verify(any());
    }

    @Test
    void verifyOtp_givenInvalidOtpCode_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/otp/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "otp_code": "",
                                   "reference_id": "c0a80123-4567-890a-bcde-f0123456789a"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0]").value("OTP code must be a string up to 6 digits"))
                .andExpect(jsonPath("$.details").doesNotExist());

        verify(otpService, never()).verify(any());
    }

    @Test
    void verifyOtp_givenInvalidReferenceId_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/otp/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "otp_code": "123456",
                                   "reference_id": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0]").value("Reference ID must not be null"))
                .andExpect(jsonPath("$.details").doesNotExist());

        verify(otpService, never()).verify(any());
    }

    @Test
    void verifyOtp_givenOtpNotFound_thenShouldReturnNotFound() throws Exception {
        doThrow(new OtpNotFoundException()).when(otpService).verify(any());

        mockMvc.perform(post("/api/v1/otp/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "otp_code": "123456",
                                   "reference_id": "c0a80123-4567-890a-bcde-f0123456789a"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Otp not found"))
                .andExpect(jsonPath("$.message").value("Resource Error"));

        verify(otpService, times(1)).verify(any());
    }
}
