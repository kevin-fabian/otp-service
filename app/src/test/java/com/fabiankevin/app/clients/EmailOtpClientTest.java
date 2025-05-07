package com.fabiankevin.app.clients;

import com.fabiankevin.app.exceptions.EmailNotificationException;
import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class EmailOtpClientTest {
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final TemplateEngine templateEngine = mock(TemplateEngine.class);
    private final OtpClient emailOtpClient = new EmailOtpClient(mailSender, templateEngine, "Subject", 10);
    private Otp mockedOtp;

    @BeforeEach
    void setup() {
        mockedOtp = Otp.builder()
                .id(UUID.randomUUID())
                .otpCode("123456")
                .userIdentifier("test@example.com")
                .purpose(OtpPurpose.LOGIN)
                .status(OtpStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .deliveryMethod(DeliveryMethod.EMAIL)
                .attemptCount(0)
                .metadata(null)
                .build();
    }

    @Test
    void send_givenValidOtp_thenShouldSendEmailSuccessfully() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(templateEngine.process(eq("otp-email-template"), any(Context.class))).thenReturn("<html>OTP Email</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailOtpClient.send(mockedOtp);

        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(eq("otp-email-template"), any(Context.class));
    }

    @Test
    void send_givenMailException_thenShouldThrowEmailSendingException() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(templateEngine.process(eq("otp-email-template"), any(Context.class))).thenReturn("<html>OTP Email</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new MailSendException("test")).when(mailSender).send(any(MimeMessage.class));

        assertThatExceptionOfType(EmailNotificationException.class)
                .isThrownBy(() -> emailOtpClient.send(mockedOtp))
                .withMessageContaining("test@example.com");

        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(eq("otp-email-template"), any(Context.class));
    }

    @Test
    void send_givenTemplateDoesNotExist_thenShouldThrowEmailSendingException() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        MimeMessageHelper mimeMessageHelper = mock(MimeMessageHelper.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("otp-email-template"), any(Context.class))).thenThrow(TemplateInputException.class);
        doThrow(MessagingException.class).when(mimeMessageHelper).setTo(anyString());

        assertThatExceptionOfType(EmailNotificationException.class)
                .isThrownBy(() -> emailOtpClient.send(mockedOtp))
                .withMessageContaining("test@example.com");

        verify(templateEngine, times(1)).process(eq("otp-email-template"), any(Context.class));
        verify(mailSender, never()).send(mimeMessage);

    }
}