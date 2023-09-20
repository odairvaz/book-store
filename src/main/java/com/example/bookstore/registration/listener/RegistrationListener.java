package com.example.bookstore.registration.listener;

import com.example.bookstore.persistense.model.User;
import com.example.bookstore.registration.OnRegistrationCompleteEvent;
import com.example.bookstore.service.IUserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final IUserService service;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final HttpServletRequest request;


    public RegistrationListener(IUserService service, JavaMailSender mailSender, TemplateEngine templateEngine, HttpServletRequest request) {
        this.service = service;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.request = request;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        service.createVerificationToken(user, token);
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());

        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation";
        String confirmationUrl = baseUrl + "/api/registrationConfirm?token=" + token;

        Context thymeleafContext = new Context(event.getLocale());
        thymeleafContext.setVariable("user", user);
        thymeleafContext.setVariable("confirmationUrl", confirmationUrl);

        String emailContent = templateEngine.process("registration/registration-email", thymeleafContext);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(emailContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
        }
    }
}
