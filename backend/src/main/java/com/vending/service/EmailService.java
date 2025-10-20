package com.vending.service;

import com.vending.config.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailProperties emailProperties;

    /**
     * Send HTML email using Thymeleaf template
     */
    @Async
    public void sendTemplateEmail(String to, String subject, String template, Map<String, Object> variables) {
        if (!emailProperties.isEnabled()) {
            log.info("Email disabled. Would have sent to {}: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom(), emailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariables(variables);
            String html = templateEngine.process(template, context);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, subject, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", to, subject, e);
        }
    }

    /**
     * Send email to multiple recipients
     */
    @Async
    public void sendTemplateEmailToMultiple(String[] recipients, String subject, String template, Map<String, Object> variables) {
        for (String recipient : recipients) {
            sendTemplateEmail(recipient, subject, template, variables);
        }
    }

    /**
     * Send simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!emailProperties.isEnabled()) {
            log.info("Email disabled. Would have sent to {}: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom(), emailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
            log.info("Simple email sent successfully to {}: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send simple email to {}: {}", to, subject, e);
        } catch (Exception e) {
            log.error("Unexpected error sending simple email to {}: {}", to, subject, e);
        }
    }
}
