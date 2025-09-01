package com.fabiankevin.app.web;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.entities.OtpTransactionEntity;
import com.fabiankevin.app.persistence.jpa.JpaOtpRepository;
import com.fabiankevin.app.services.totp.TotpCodeVerifier;
import com.fabiankevin.app.services.totp.TotpService;
import com.fabiankevin.app.services.totp.commands.RegisterTotpCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TotpControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    @Autowired
    private TotpService totpService;

    @MockitoBean
    private TotpCodeVerifier totpCodeVerifier;

    @Autowired
    private JpaOtpRepository jpaOtpRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Jwt mockJwt;

    private final static UUID USER_PROFILE_ID = UUID.randomUUID();

    @BeforeEach
    void reset() {
        jpaOtpRepository.deleteAll();

        mockJwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .header("kid", UUID.randomUUID().toString())
                .claim("sub", USER_PROFILE_ID)
                .claim("scope", "totp:manage")
                .expiresAt(Instant.now().plusSeconds(300))
                .issuedAt(Instant.now())
                .build();

        when(jwtDecoder.decode(anyString())).thenReturn(mockJwt);
    }

    @Test
    void register_givenValidRequest_thenShouldReturnTotpResponse() throws Exception {
        mockMvc.perform(post("/v1/totp/users")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {
                                    "user_reference_id": "{{user_profile_id}}"
                                 }
                                """.replace("{{user_profile_id}}", USER_PROFILE_ID.toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void register_givenInvalidRequest_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/totp/users")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0]").value("User reference ID must not be blank"));
    }

    @Test
    void register_givenNullRequest_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/totp/users")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQrCodeImage_givenValidUserReferenceId_thenShouldReturnQrCodeImage() throws Exception {
        String userProfileId = UUID.randomUUID().toString();
        TotpUser totpUser = totpService.registerTotp(RegisterTotpCommand.builder()
                .userProfileId(userProfileId)
                .build());

        mockMvc.perform(get("/v1/totp/users/{id}/qr", totpUser.id())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals(MediaType.IMAGE_PNG_VALUE, result.getResponse().getContentType(), "Content type should be PNG image"));
    }

    @Test
    void getQrCodeImage_givenInvalidUserReferenceId_thenShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/v1/totp/users/{id}/qr", UUID.randomUUID())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Unregistered"));
    }

    @Test
    void verify_givenValidRequest_thenShouldVerifySuccessfully() throws Exception {
        String userProfileId = UUID.randomUUID().toString();
        TotpUser totpUser = totpService.registerTotp(RegisterTotpCommand.builder()
                .userProfileId(userProfileId)
                .build());

        when(totpCodeVerifier.verify(any(), eq("123456"))).thenReturn(true);

        mockMvc.perform(post("/v1/totp/users/{id}/verify", totpUser.id())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "123456",
                                    "purpose": "LOGIN"
                                }
                                """))
                .andExpect(status().isOk());

        verify(totpService, times(1)).verify(any());

        OtpTransactionEntity otpTransactionEntity = jpaOtpRepository.findAll().getFirst();
        assertEquals(OtpStatus.VERIFIED, otpTransactionEntity.getStatus(), "Otp status should be VERIFIED");
        assertEquals(0, otpTransactionEntity.getAttemptCount(), "attempt count should be 0");
        assertEquals("123456", otpTransactionEntity.getOtpCode(), "otp code should match");
        assertEquals(userProfileId, otpTransactionEntity.getRecipient(), "user profile id should match");
        assertEquals(DeliveryMethod.TOTP, otpTransactionEntity.getDeliveryMethod(), "user profile id should match");
        assertEquals(OtpPurpose.LOGIN, otpTransactionEntity.getPurpose(), "purpose should be LOGIN");
    }

    @Test
    void verify_givenIncorrectOnFirstAttempt_thenAttemptCountShouldBeOneAndStatusShouldBeActive() throws Exception {
        String userProfileId = UUID.randomUUID().toString();
        TotpUser totpUser = totpService.registerTotp(RegisterTotpCommand.builder()
                .userProfileId(userProfileId)
                .build());

        when(totpCodeVerifier.verify(any(), eq("123456"))).thenReturn(false);

        mockMvc.perform(post("/v1/totp/users/{id}/verify", totpUser.id())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "123452",
                                    "purpose": "LOGIN"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(totpService, times(1)).verify(any());

        OtpTransactionEntity otpTransactionEntity = jpaOtpRepository.findAll().getFirst();
        assertEquals(OtpStatus.ACTIVE, otpTransactionEntity.getStatus(), "Otp status should be ACTIVE");
        assertEquals(1, otpTransactionEntity.getAttemptCount(), "attempt count should be 0");
    }

    @Test
    void verify_givenInvalidRequest_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/totp/users/{id}/verify", UUID.randomUUID())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    void verify_givenUnregisteredUser_thenShouldReturnNotFound() throws Exception {
        mockMvc.perform(post("/v1/totp/users/{id}/verify", UUID.randomUUID())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "123456",
                                    "purpose": "LOGIN"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Unregistered"));
    }
}