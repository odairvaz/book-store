package com.example.bookstore.web.controller;


import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.model.VerificationToken;
import com.example.bookstore.registration.OnRegistrationCompleteEvent;
import com.example.bookstore.security.Token;
import com.example.bookstore.security.TokenWrapper;
import com.example.bookstore.service.IUserService;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/api")
public class RegistrationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);
    private static final String REGISTRATION_PAGE = "registration/registration";
    private static final String SUCCESS_REGISTER_PAGE = "registration/success-register";
    private static final String SUCCESS_REGENERATE_TOKEN_PAGE = "registration/success-regenerate-token";
    private static final String BAD_USER_PAGE = "registration/bad-user";
    private static final String ERROR_REGISTRATION_PAGE = "registration/error-registration";
    private static final String REDIRECT_LOGIN = "redirect:/login.html?lang=";
    private static final String INVALID_TOKEN = "invalid_token";
    private static final String EXPIRED_TOKEN = "expired_token";
    private final IUserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource messages;
    private final JavaMailSender mailSender;

    public RegistrationController(IUserService userService, ApplicationEventPublisher eventPublisher, MessageSource messages, JavaMailSender mailSender) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.messages = messages;
        this.mailSender = mailSender;
    }

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDto());
        return REGISTRATION_PAGE;
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto, BindingResult bindingResult, HttpServletRequest request, Model model) {

        if (bindingResult.hasErrors()) {
            return REGISTRATION_PAGE;
        }

        try {
            User newUserAccount = userService.registerNewUserAccount(userDto);
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUserAccount, request.getLocale(), appUrl, null));
        } catch (UserAlreadyExistException uaeEx) {
            bindingResult.rejectValue("email", "error.user", "There is already a user registered with the email provided.");
            return REGISTRATION_PAGE;
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to register user", ex);
            model.addAttribute("errorMessage", "An error occurred when sending the email!");
            return ERROR_REGISTRATION_PAGE;
        }
        model.addAttribute("successMessage", messages.getMessage("message.register.success", null, request.getLocale()));
        return SUCCESS_REGISTER_PAGE;
    }

    @GetMapping("/registration-confirm")
    public String confirmRegistration(HttpServletRequest request, @RequestParam("token") String token) {
        Locale locale = request.getLocale();
        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            return redirectToBadUser(token, INVALID_TOKEN, locale);
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return redirectToBadUser(token, EXPIRED_TOKEN, locale);
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        return "redirect:/api/success-register?lang=" + locale.getLanguage();
    }

    @GetMapping("/resend-registration-token")
    public String resendRegistrationToken(@RequestParam("token") String existingToken, HttpServletRequest request) {
        Locale locale = request.getLocale();
        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
        if (newToken == null) {
            return redirectToBadUser(existingToken, INVALID_TOKEN, locale);
        }

        User user = userService.getUser(newToken.getToken());
        String appUrl = request.getContextPath();
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl, newToken.getToken()));
        return "redirect:/api/success-regenerate-token?lang=" + locale.getLanguage();
    }

    @GetMapping("/success-register")
    public String successRegister(Model model, HttpServletRequest request) {
        model.addAttribute("successMessage", messages.getMessage("message.activated.success", null, request.getLocale()));
        return SUCCESS_REGISTER_PAGE;
    }

    @GetMapping("/success-regenerate-token")
    public String successRegenerateNewToken() {
        return SUCCESS_REGENERATE_TOKEN_PAGE;
    }

    @GetMapping("/bad-user")
    public String badUser(@RequestParam("error") String errorCode, Model model, HttpServletRequest request) {
        VerificationToken verificationToken = userService.getVerificationToken(request.getParameter("token"));
        String errorMessage = getErrorCode(errorCode, request.getLocale());
        model.addAttribute("isTokenValid", true);
        if (verificationToken == null) {
            model.addAttribute("isTokenValid", false);
        }

        model.addAttribute("errorMessage", errorMessage);
        return BAD_USER_PAGE;
    }

    @GetMapping("/forget-password")
    public String showForgetPasswordPage() {
        return "password/forgot-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(HttpServletRequest request, Model model, @RequestParam("email") String userEmail) {
        final User user = userService.findUserByEmail(userEmail);
        if (user == null) {
            return "redirect:/password/forgot-password?lang=" + request.getLocale().getLanguage();
        }

        final String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user, token);
        try {
            String appUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
            final SimpleMailMessage email = constructResetTokenEmail(appUrl, request.getLocale(), token, user);
            mailSender.send(email);
        } catch (final MailAuthenticationException e) {
            LOGGER.debug("MailAuthenticationException", e);
            return "redirect:/emailError.html?lang=" + request.getLocale().getLanguage();
        } catch (final Exception e) {
            LOGGER.debug(e.getLocalizedMessage(), e);
            model.addAttribute("message", e.getLocalizedMessage());
            return REDIRECT_LOGIN + request.getLocale().getLanguage();
        }
        return "redirect:login?lang=" + request.getLocale().getLanguage();
    }

    @GetMapping("/update-password")
    public String showChangePassword(Locale locale, @RequestParam("token") String token, Model model) {
        Token tk = new TokenWrapper(userService.getPasswordResetToken(token));
        if (tk.isTokenFound() && tk.isTokenExpired()) {
            return REDIRECT_LOGIN + locale.getLanguage();
        }
        model.addAttribute("token", token);
        return "password/update-password";
    }

    @PostMapping("/save-password")
    public String savePassword(@RequestParam("password") String password, @RequestParam("token") String token) {
        User user = userService.getPasswordResetToken(token).getUser();
        userService.changeUserPassword(user, password);
        return "redirect:login?lang=" + Locale.getDefault().getLanguage();
    }

    private String redirectToBadUser(String token, String errorCode, Locale locale) {
        return "redirect:/api/bad-user?lang=" + locale.getLanguage() + "&token=" + token + "&error=" + errorCode;
    }

    private String getErrorCode(String errorCode, Locale locale) {
        return switch (errorCode) {
            case EXPIRED_TOKEN -> messages.getMessage("auth.message.expired", null, locale);
            case INVALID_TOKEN -> messages.getMessage("auth.message.invalidToken", null, locale);
            default -> "An error occurred.";
        };
    }

    private SimpleMailMessage constructResetTokenEmail(final String contextPath, final Locale locale, final String token, final User user) {
        final String url = contextPath + "/api/update-password?token=" + token;
        final String message = messages.getMessage("mail.reset.password", new Object[]{user.getFirstName(), url}, locale);
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject("Reset Password");
        email.setText(message);
        return email;
    }

}
