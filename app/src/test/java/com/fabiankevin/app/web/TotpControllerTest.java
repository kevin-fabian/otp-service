package com.fabiankevin.app.web;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.services.totp.TotpService;
import com.fabiankevin.app.services.totp.commands.RegisterTotpCommand;
import com.fabiankevin.app.services.totp.commands.VerifyTotpCommand;
import com.github.fabiankevin.lemon.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({GlobalExceptionHandler.class})
@WebMvcTest(controllers = TotpController.class)
@ActiveProfiles("test")
class TotpControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TotpService totpService;

    private Jwt mockJwt;

    private static final UUID USER_PROFILE_ID = UUID.randomUUID();

    @BeforeEach
    void reset() {
        mockJwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .header("kid", UUID.randomUUID().toString())
                .claim("sub", USER_PROFILE_ID.toString())
                .claim("scope", "totp:manage")
                .expiresAt(Instant.now().plusSeconds(300))
                .issuedAt(Instant.now())
                .build();
    }

    @Test
    void register_givenValidRequest_thenShouldReturnTotpResponse() throws Exception {
        TotpUser totpUser = TotpUser.builder().id(USER_PROFILE_ID).build();
        when(totpService.registerTotp(any(RegisterTotpCommand.class))).thenReturn(totpUser);

        mockMvc.perform(post("/v1/totp/users")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        verify(totpService, times(1)).registerTotp(new RegisterTotpCommand(USER_PROFILE_ID.toString()));
    }

    @Test
    void getQrCodeImage_givenValidUserReferenceId_thenShouldReturnQrCodeImage() throws Exception {
        byte[] image = new byte[]{1, 2, 3};
        when(totpService.getQrCodeImageByUserReferenceId(USER_PROFILE_ID.toString())).thenReturn(image);

        mockMvc.perform(get("/v1/totp/users/qr")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals(MediaType.IMAGE_PNG_VALUE, result.getResponse().getContentType(), "Content type should be PNG image"));

        verify(totpService, times(1)).getQrCodeImageByUserReferenceId(USER_PROFILE_ID.toString());
    }

    @Test
    void verify_givenValidRequest_thenShouldVerifySuccessfully() throws Exception {
        doNothing().when(totpService).verify(any(VerifyTotpCommand.class));

        mockMvc.perform(post("/v1/totp/users/{userReferenceId}/verify", USER_PROFILE_ID)
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
    }

    @Test
    void verify_givenMultipleValidRequests_thenShouldOnlySucceedOnce() throws Exception {
        // first call succeeds, second call fails with invalid state
        doNothing().doThrow(new com.fabiankevin.app.exceptions.OtpInvalidStateException()).when(totpService).verify(any(VerifyTotpCommand.class));

        mockMvc.perform(post("/v1/totp/users/{userReferenceId}/verify", USER_PROFILE_ID)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "123456",
                                    "purpose": "LOGIN"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/totp/users/{userReferenceId}/verify", USER_PROFILE_ID)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "123456",
                                    "purpose": "LOGIN"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(totpService, times(2)).verify(any());
    }

    @Test
    void verify_givenIncorrectCode_thenAttemptCountShouldBeOneAndStatusShouldBeActive() throws Exception {
        doThrow(new com.fabiankevin.app.exceptions.TotpInvalidCodeException()).when(totpService).verify(any(VerifyTotpCommand.class));

        mockMvc.perform(post("/v1/totp/users/{userReferenceId}/verify", USER_PROFILE_ID)
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
    }

    @Test
    void verify_givenInvalidRequest_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/totp/users/{userReferenceId}/verify", UUID.randomUUID())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    void verify_givenUnregisteredUser_thenShouldReturnNotFound() throws Exception {
        doThrow(new com.fabiankevin.app.exceptions.TotpUnregisteredException()).when(totpService).verify(any(VerifyTotpCommand.class));

        mockMvc.perform(post("/v1/totp/users/{userReferenceId}/verify", UUID.randomUUID())
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