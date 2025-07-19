package com.fabiankevin.app.web;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
}