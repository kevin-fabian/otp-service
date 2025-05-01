package com.fabiankevin.app.clients;

import com.fabiankevin.app.exceptions.EmailSendingException;
import com.fabiankevin.app.models.Otp;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@RequiredArgsConstructor
public class EmailOtpClient implements OtpClient {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private String fromEmail;
    private String subject;
    private int expirationMinutes;

    @Override
    public void send(Otp otp) {
        String to = otp.userIdentifier();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("otpCode", otp.otpCode());
            context.setVariable("expirationMinutes", expirationMinutes);
            String htmlContent = templateEngine.process("otp-email-template", context);

            helper.setTo(to);
            helper.setSubject("Test Subject");
            helper.setText(htmlContent, true); // true indicates HTML content

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MailException | MessagingException exception) {
            log.error("Error sending email to {}", to, exception);
            throw new EmailSendingException(to, exception);
        }
    }
}
