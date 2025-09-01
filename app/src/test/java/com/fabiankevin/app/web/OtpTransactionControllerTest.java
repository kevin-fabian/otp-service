package com.fabiankevin.app.web;

import com.fabiankevin.app.clients.EmailOtpClient;
import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.services.otp.OtpService;
import com.fabiankevin.app.services.otp.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.otp.commands.VerifyOtpCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OtpTransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    @Autowired
    private OtpService otpService;

    @MockitoSpyBean
    @Autowired
    private OtpTransactionRepository otpTransactionRepository;

    @MockitoBean
    private EmailOtpClient emailOtpClient;

    @MockBean
    private JwtDecoder jwtDecoder;

    private OtpTransaction mockedOtpTransaction;
    private GenerateOtpCommand generateOtpCommand;
    private Jwt mockJwt;

    @BeforeEach
    void setup() {
        OffsetDateTime now = OffsetDateTime.now();
        mockedOtpTransaction = OtpTransaction.builder()
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipient("test@test.com")
                .status(OtpStatus.ACTIVE)
                .metadata("test metadata")
                .attemptCount(0)
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(now.plusMinutes(1))
                .otpCode("123456")
                .build();
        generateOtpCommand = GenerateOtpCommand.builder()
                .deliveryMethod(DeliveryMethod.EMAIL)
                .purpose(OtpPurpose.LOGIN)
                .recipient("john.doe@test.com")
                .metadata("some metadata")
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

        when(jwtDecoder.decode(anyString())).thenReturn(mockJwt);
    }

    @Test
    void generateOtp_givenValidRequest_thenShouldReturnOtpCode() throws Exception {
        mockMvc.perform(post("/v1/otps")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "recipient": "test@test.com",
                                   "purpose": "LOGIN",
                                   "delivery_method": "EMAIL",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.recipient").value("test@test.com"))
                .andExpect(jsonPath("$.purpose").value("LOGIN"))
                .andExpect(jsonPath("$.delivery_method").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.metadata").value("test metadata"))
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.updated_at").exists())
                .andExpect(jsonPath("$.expired_at").exists());

        verify(emailOtpClient, times(1)).send(any());
        verify(otpService, times(1)).generate(any());
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
                                   "delivery_method": "EMAIL",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details").value("Malformed JSON request"));

        verify(otpService, never()).generate(any());
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
                                   "delivery_method": "INVALID_METHOD",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details").value("Malformed JSON request"));

        verify(otpService, never()).generate(any());
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
                                   "delivery_method": "EMAIL",
                                   "metadata": "test metadata"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(otpService, never()).generate(any());
    }

    @Test
    void verifyOtp_givenValidRequest_thenShouldPassVerification() throws Exception {
        OtpTransaction generatedOtpTransaction = otpService.generate(generateOtpCommand);

        mockMvc.perform(post("/v1/otps/{otp}/verify", generatedOtpTransaction.id())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "otp_code": "{{otp_code}}"
                                }
                                """.replace("{{otp_code}}", generatedOtpTransaction.otpCode())))
                .andExpect(status().isNoContent());

        verify(otpService, times(1)).verify(any());

        OtpTransaction otpTransaction = otpTransactionRepository.retrieveById(generatedOtpTransaction.id()).get();
        assertEquals(OtpStatus.VERIFIED, otpTransaction.status(), "Otp status should be VERIFIED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1", "123456"})
    void verifyOtp_givenInvalidOtpCode_thenShouldReturnBadRequest(String invalidOtpCode) throws Exception {
        OtpTransaction generatedOtpTransaction = otpService.generate(generateOtpCommand);

        mockMvc.perform(post("/v1/otps/{id}/verify", generatedOtpTransaction.id())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "otp_code": "{{otp_code}}"
                                }
                                """.replace("{{otp_code}}", invalidOtpCode)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyOtp_givenOtpNotFound_thenShouldReturnNotFound() throws Exception {
        otpService.generate(generateOtpCommand);

        mockMvc.perform(post("/v1/otps/{id}/verify", UUID.randomUUID())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "otp_code": "123456"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Otp not found"))
                .andExpect(jsonPath("$.message").value("Resource Error"));
    }

    @Test
    void retrieveById_givenValidId_thenShouldReturnOtpDetails() throws Exception {
        OtpTransaction savedOtpTransaction = otpTransactionRepository.save(mockedOtpTransaction);

        mockMvc.perform(get("/v1/otps/{id}", savedOtpTransaction.id())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedOtpTransaction.id().toString()))
                .andExpect(jsonPath("$.recipient").value(mockedOtpTransaction.recipient()))
                .andExpect(jsonPath("$.purpose").value(mockedOtpTransaction.purpose().name()))
                .andExpect(jsonPath("$.delivery_method").value(mockedOtpTransaction.deliveryMethod().name()))
                .andExpect(jsonPath("$.status").value(mockedOtpTransaction.status().name()))
                .andExpect(jsonPath("$.updated_at").exists())
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.expired_at").exists());

        verify(otpService, times(1)).retrieveById(savedOtpTransaction.id());
    }

    @Test
    void retrieveById_givenInvalidId_thenShouldReturnNotFound() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(get("/v1/otps/{id}", invalidId)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource Error"))
                .andExpect(jsonPath("$.details").value("Otp not found"));

        verify(otpService, times(1)).retrieveById(invalidId);
    }

    @Test
    void markOtpAsUsed_givenVerifiedOtp_thenShouldUpdateStatusToUsedAndReturnNoContent() throws Exception {
        OtpTransaction generatedOtpTransaction = otpService.generate(generateOtpCommand);
        otpService.verify(new VerifyOtpCommand(generatedOtpTransaction.id(), generatedOtpTransaction.otpCode()));

        mockMvc.perform(patch("/v1/otps/{id}/mark-as-used", generatedOtpTransaction.id())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(otpService, times(1)).markAsUsed(generatedOtpTransaction.id());

        OtpTransaction otpTransaction = otpTransactionRepository.retrieveById(generatedOtpTransaction.id()).get();
        assertEquals(OtpStatus.USED, otpTransaction.status(), "Otp status should be USED");
    }

    @Test
    void markOtpAsUsed_givenOtpIsNotVerified_thenShouldBeBadRequest() throws Exception {
        OtpTransaction generatedOtpTransaction = otpService.generate(generateOtpCommand);

        mockMvc.perform(patch("/v1/otps/{id}/mark-as-used", generatedOtpTransaction.id())
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(otpService, times(1)).markAsUsed(generatedOtpTransaction.id());
    }

    @Test
    void markOtpAsUsed_givenInvalidId_thenShouldReturnNotFound() throws Exception {
        OtpTransaction generatedOtpTransaction = otpService.generate(generateOtpCommand);
        otpService.verify(new VerifyOtpCommand(generatedOtpTransaction.id(), generatedOtpTransaction.otpCode()));

        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(patch("/v1/otps/{id}/mark-as-used", invalidId)
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource Error"))
                .andExpect(jsonPath("$.details").value("Otp not found"));
    }
}
