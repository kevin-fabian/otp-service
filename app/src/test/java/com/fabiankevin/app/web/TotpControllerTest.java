package com.fabiankevin.app.web;

import com.fabiankevin.app.exceptions.TotpUnregisteredException;
import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.services.TotpService;
import com.fabiankevin.app.web.dtos.TotpResponse;
import com.github.fabiankevin.microwebspringbootstarter.MicrowebAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TotpController.class)
@Import({MicrowebAutoConfiguration.class})
class TotpControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TotpService totpService;

    private TotpUser registeredTotpUser;

    @BeforeEach
    void setUp() {
        // This method can be used to set up common test data or mock behaviors if needed
        registeredTotpUser = TotpUser.builder()
                .userReferenceId("john.doe@test.com")
                .build();
    }

    @Test
    void register_givenValidRequest_thenShouldReturnTotpResponse() throws Exception {
        TotpResponse response = new TotpResponse("validUserReferenceId");

        when(totpService.registerTotp(any()))
                .thenReturn(registeredTotpUser);

        mockMvc.perform(post("/api/v1/totp/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"user_reference_id": "validUserReferenceId"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_reference_id").value(registeredTotpUser.userReferenceId()));

        assertNotNull(response, "Response should not be null");
    }

    @Test
    void register_givenInvalidRequest_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/totp/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0]").value("User reference ID must not be blank"));
    }

    @Test
    void register_givenNullRequest_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/totp/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQrCodeImage_givenValidUserReferenceId_thenShouldReturnQrCodeImage() throws Exception {
        String validUserReferenceId = "validUserReferenceId";
        byte[] qrCodeImage = "sampleQrCodeImage".getBytes();


        when(totpService.getQrCodeImageByUserReferenceId(validUserReferenceId)).thenReturn(qrCodeImage);

        mockMvc.perform(get("/api/v1/totp/qr/{userReferenceId}", validUserReferenceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals(MediaType.IMAGE_PNG_VALUE, result.getResponse().getContentType(), "Content type should be PNG image"))
                .andExpect(result -> assertArrayEquals(qrCodeImage, result.getResponse().getContentAsByteArray(), "QR code image content should match"));
    }

    @Test
    void getQrCodeImage_givenInvalidUserReferenceId_thenShouldReturnNotFound() throws Exception {
        String invalidUserReferenceId = "invalidUserReferenceId";

        when(totpService.getQrCodeImageByUserReferenceId(invalidUserReferenceId)).thenThrow(new TotpUnregisteredException());

        mockMvc.perform(get("/api/v1/totp/qr/" + invalidUserReferenceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Unregistered"));
    }

    @Test
    void verify_givenValidRequest_thenShouldVerifySuccessfully() throws Exception {
        mockMvc.perform(post("/v1/totp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "user_reference_id": "validUserReferenceId",
                                    "code": "123456"
                                }
                                """))
                .andExpect(status().isOk());

        verify(totpService, times(1)).verifyTotp("validUserReferenceId", "123456");
    }

    @Test
    void verify_givenInvalidRequest_thenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/totp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    void verify_givenUnregisteredUser_thenShouldReturnNotFound() throws Exception {
        String invalidUserReferenceId = "invalidUserReferenceId";
        String code = "654321";

        doThrow(new TotpUnregisteredException()).when(totpService).verifyTotp(invalidUserReferenceId, code);

        mockMvc.perform(post("/v1/totp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "user_reference_id": "invalidUserReferenceId",
                                    "code": "654321"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Unregistered"));

        verify(totpService, times(1)).verifyTotp(invalidUserReferenceId, code);
    }
}