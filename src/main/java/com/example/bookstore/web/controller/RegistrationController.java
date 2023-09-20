package com.example.bookstore.web.controller;


import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.model.VerificationToken;
import com.example.bookstore.registration.OnRegistrationCompleteEvent;
import com.example.bookstore.service.IUserService;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Locale;

@Controller
@RequestMapping("/api")
public class RegistrationController {

    private static final String REGISTRATION_PAGE = "registration/registration";
    private static final String SUCCESS_REGISTER_PAGE = "registration/success-register";
    private static final String BAD_USER_PAGE = "registration/bad-user";
    private static final String ERROR_REGISTRATION_PAGE = "registration/error-registration";
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final IUserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource messages;

    public RegistrationController(IUserService userService, ApplicationEventPublisher eventPublisher, MessageSource messages) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.messages = messages;
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
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUserAccount, request.getLocale(), appUrl));
        } catch (UserAlreadyExistException uaeEx) {
            bindingResult.rejectValue("email", "error.user", "There is already a user registered with the email provided.");
            return REGISTRATION_PAGE;
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to register user", ex);
            model.addAttribute("errorMessage", "An error occurred during registration. Please try again!");
            return ERROR_REGISTRATION_PAGE;
        }
        model.addAttribute("successMessage", messages.getMessage("message.register.success", null, request.getLocale()));
        return SUCCESS_REGISTER_PAGE;
    }

    @GetMapping("/registrationConfirm")
    public String confirmRegistration(HttpServletRequest request, @RequestParam("token") String token) {
        Locale locale = request.getLocale();
        VerificationToken verificationToken = userService.getVerificationToken(token);

        if (verificationToken == null) {
            return redirectToBadUser("invalid_token", locale);
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();

        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return redirectToBadUser("expired_token", locale);
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        return "redirect:/api/success-register?lang=" + locale.getLanguage();
    }

    @GetMapping("/success-register")
    public String successRegister(Model model, HttpServletRequest request) {
        model.addAttribute("successMessage", messages.getMessage("message.activated.success", null, request.getLocale()));
        return SUCCESS_REGISTER_PAGE;
    }

    @GetMapping("/bad-user")
    public String badUser(@RequestParam("error") String errorCode, Model model, Locale locale) {
        String errorMessage = getErrorCode(errorCode, locale);
        model.addAttribute("errorMessage", errorMessage);
        return BAD_USER_PAGE;
    }

    private String redirectToBadUser(String errorCode, Locale locale) {
        return "redirect:/api/bad-user?lang=" + locale.getLanguage() + "&error=" + errorCode;
    }

    private String getErrorCode(String errorCode, Locale locale) {
        return switch (errorCode) {
            case "expired_token" -> messages.getMessage("auth.message.expired", null, locale);
            case "invalid_token" -> messages.getMessage("auth.message.invalidToken", null, locale);
            default -> "An error occurred.";
        };
    }

}
