package com.example.bookstore.registration.listener;

import com.example.bookstore.persistense.model.User;
import com.example.bookstore.registration.OnRegistrationCompleteEvent;
import com.example.bookstore.service.IUserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final IUserService service;
    private final MessageSource messages;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;


    public RegistrationListener(IUserService service, MessageSource messages, JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.service = service;
        this.messages = messages;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        service.createVerificationToken(user, token);

        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation";
        String confirmationUrl = event.getAppUrl() + "/api/registrationConfirm?token=" + token;

        Context thymeleafContext = new Context(event.getLocale());
        thymeleafContext.setVariable("user", user);
        thymeleafContext.setVariable("message", messages.getMessage("message.regSucc", null, event.getLocale()));
        thymeleafContext.setVariable("confirmationUrl", confirmationUrl);

        String emailContent = templateEngine.process("registration-email", thymeleafContext);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(emailContent, true);

            // Send the HTML email
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Handle the exception
        }


        /*String message = messages.getMessage("message.regSucc", null, event.getLocale());

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + "\r\n" + "http://localhost:8080" + confirmationUrl);
        mailSender.send(email);*/
    }
}
