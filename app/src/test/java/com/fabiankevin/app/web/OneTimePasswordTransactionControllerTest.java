package com.fabiankevin.app.web;

import com.fabiankevin.app.exceptions.InvalidOtpException;
import com.fabiankevin.app.exceptions.OtpInvalidStateException;
import com.fabiankevin.app.exceptions.OtpNotFoundException;
import com.fabiankevin.app.models.OneTimePasswordTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.services.otp.OneTimePasswordService;
import com.fabiankevin.app.services.otp.commands.VerifyOtpCommand;
import com.github.fabiankevin.lemon.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({GlobalExceptionHandler.class})
@WebMvcTest(controllers = OneTimePasswordController.class)
@ActiveProfiles("test")
class OneTimePasswordTransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OneTimePasswordService oneTimePasswordService;

    private OneTimePasswordTransaction mockedOneTimePasswordTransaction;
    private Jwt mockJwt;

    @BeforeEach
    void setup() {
        OffsetDateTime now = OffsetDateTime.now();
        mockedOneTimePasswordTransaction = OneTimePasswordTransaction.builder()
                .id(UUID.randomUUID())
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipient("test@test.com")
                .status(OtpStatus.SENT)
                .metadata("test metadata")
                .attemptCount(0)
                .createdAt(now.toInstant())
                .updatedAt(now.toInstant())
                .expiresAt(now.plusMinutes(1))
                .otpCode("123456")
                .build();

        mockJwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .header("kid", UUID.randomUUID().toString())
                .claim("sub", "test-user")
                .claim("scope", "otp:manage")
                .claim("roles", Collections.singletonList("USER"))
                .expiresAt(Instant.now().plusSeconds(300))
                .issuedAt(Instant.now())
                .build();
    }

    @Test
    void generateOtp_givenValidRequest_thenShouldReturnOtpCode() throws Exception {
        when(oneTimePasswordService.generate(any())).thenReturn(mockedOneTimePasswordTransaction);
        mockMvc.perform(post("/v1/otps")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "recipient": "test@test.com",
                                   "purpose": "LOGIN",
                                   "deliveryMethod": "EMAIL",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.recipient").value("test@test.com"))
                .andExpect(jsonPath("$.purpose").value("LOGIN"))
                .andExpect(jsonPath("$.deliveryMethod").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.metadata").value("test metadata"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.expiredAt").exists());

        verify(oneTimePasswordService, times(1)).generate(any());
    }

   @Test
    void generateOtp_givenInvalidPurpose_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/otps")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "recipient": "test@test.com",
                                   "purpose": "INVALID_PURPOSE",
                                   "deliveryMethod": "EMAIL",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.details").value("The request body is not properly formatted or contains invalid JSON"));

        verify(oneTimePasswordService, never()).generate(any());
    }

    @Test
    void generateOtp_givenInvalidDeliveryMethod_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/otps")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "recipient": "test@test.com",
                                   "purpose": "LOGIN",
                                   "deliveryMethod": "INVALID_METHOD",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.details").value("The request body is not properly formatted or contains invalid JSON"));

        verify(oneTimePasswordService, never()).generate(any());
    }

    @Test
    void generateOtp_givenInvalidRecipient_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/otps")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "recipient": "",
                                   "purpose": "LOGIN",
                                   "deliveryMethod": "EMAIL",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request parameters"));

        verify(oneTimePasswordService, never()).generate(any());
    }


    @Test
    void verifyOtp_givenValidRequest_thenShouldPassVerification() throws Exception {
        UUID otpId = UUID.randomUUID();
        String otpCode = "123456";
        VerifyOtpCommand verifyOtpCommand = new VerifyOtpCommand(otpId, otpCode);
        doNothing().when(oneTimePasswordService).verify(verifyOtpCommand);

        mockMvc.perform(post("/v1/otps/{otp}/verify", otpId)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "code": "{{otp_code}}"
                                }
                                """.replace("{{otp_code}}", otpCode)))
                 .andExpect(status().isNoContent());

        verify(oneTimePasswordService, times(1)).verify(verifyOtpCommand);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1", "12345", "1234567", "abcdef"})
    void verifyOtp_givenInvalidOtpCode_thenShouldReturnBadRequest(String invalidOtpCode) throws Exception {
        UUID otpId = UUID.randomUUID();
        doThrow(InvalidOtpException.class)
                .when(oneTimePasswordService).verify(new VerifyOtpCommand(otpId, invalidOtpCode));

        mockMvc.perform(post("/v1/otps/{id}/verify", otpId)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "code": "{{otp_code}}"
                                }
                                """.replace("{{otp_code}}", invalidOtpCode)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request parameters"));
    }

    @Test
    void verifyOtp_givenOtpNotFound_thenShouldReturnNotFound() throws Exception {
        doThrow(new OtpNotFoundException())
                .when(oneTimePasswordService).verify(any());

        mockMvc.perform(post("/v1/otps/{id}/verify", UUID.randomUUID())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "code": "123456"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Otp not found"))
                .andExpect(jsonPath("$.title").value("Domain error"))
                .andExpect(jsonPath("$.code").value("AUTH_OTP_404"));
    }

    @Test
    void retrieveById_givenValidId_thenShouldReturnOtpDetails() throws Exception {
        UUID otpId = mockedOneTimePasswordTransaction.id();
        when(oneTimePasswordService.retrieveById(otpId)).thenReturn(mockedOneTimePasswordTransaction);
        mockMvc.perform(get("/v1/otps/{id}", otpId)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(otpId.toString()))
                .andExpect(jsonPath("$.recipient").value(mockedOneTimePasswordTransaction.recipient()))
                .andExpect(jsonPath("$.purpose").value(mockedOneTimePasswordTransaction.purpose().name()))
                .andExpect(jsonPath("$.deliveryMethod").value(mockedOneTimePasswordTransaction.deliveryMethod().name()))
                .andExpect(jsonPath("$.status").value(mockedOneTimePasswordTransaction.status().name()))
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.expiredAt").exists());

        verify(oneTimePasswordService, times(1)).retrieveById(otpId);
    }

    @Test
    void retrieveById_givenNotFoundOtp_thenShouldReturnNotFound() throws Exception {
        UUID invalidId = UUID.randomUUID();
        when(oneTimePasswordService.retrieveById(invalidId)).thenThrow(new OtpNotFoundException());

        mockMvc.perform(get("/v1/otps/{id}", invalidId)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Domain error"))
                .andExpect(jsonPath("$.details").value("Otp not found"))
                .andExpect(jsonPath("$.code").value("AUTH_OTP_404"));

        verify(oneTimePasswordService, times(1)).retrieveById(invalidId);
    }

    @Test
    void useOtp_givenVerifiedOtp_thenShouldUpdateStatusToUsedAndReturnNoContent() throws Exception {
        UUID otpId = UUID.randomUUID();

        mockMvc.perform(patch("/v1/otps/{id}/use", otpId)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(oneTimePasswordService, times(1)).useOtp(otpId);
    }

    @Test
    void useOtp_givenOtpIsNotVerified_thenShouldBeBadRequest() throws Exception {
        UUID otpId = UUID.randomUUID();
        doThrow(new OtpInvalidStateException()).when(oneTimePasswordService).useOtp(otpId);

        mockMvc.perform(patch("/v1/otps/{id}/use", otpId)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(oneTimePasswordService, times(1)).useOtp(otpId);
    }

    @Test
    void useOtp_givenInvalidId_thenShouldReturnNotFound() throws Exception {
        UUID invalidId = UUID.randomUUID();
        doThrow(new OtpNotFoundException()).when(oneTimePasswordService).useOtp(invalidId);

        mockMvc.perform(patch("/v1/otps/{id}/use", invalidId)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Domain error"))
                .andExpect(jsonPath("$.details").value("Otp not found"));
    }
}
