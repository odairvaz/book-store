package com.example.bookstore.web.controller;


import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.model.VerificationToken;
import com.example.bookstore.registration.OnRegistrationCompleteEvent;
import com.example.bookstore.service.IUserService;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    public static final String REGISTRATION_PAGE = "registration/registration";
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
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return REGISTRATION_PAGE;
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto,BindingResult bindingResult, HttpServletRequest request, Model model) {
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
        }
        model.addAttribute("successMessage", messages.getMessage("message.register.success", null, request.getLocale()));
        return "registration/success-register";
    }

    @GetMapping("/registrationConfirm")
    public String confirmRegistration(HttpServletRequest request, @RequestParam("token") String token, Model model) {
        Locale locale = request.getLocale();

        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            return "redirect:/api/bad-user?lang=" + locale.getLanguage() + "&error=invalid_token";
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return "redirect:/api/bad-user?lang=" + locale.getLanguage() + "&error=expired_token";
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        return "redirect:/api/success-register?lang=" + locale.getLanguage();
    }

    @GetMapping("/success-register")
    public String successRegister(Model model, HttpServletRequest request) {
        model.addAttribute("successMessage", messages.getMessage("message.activated.success", null, request.getLocale()));
        return "registration/success-register";
    }

    @GetMapping("/bad-user")
    public String badUser(@RequestParam("error") String errorCode, Model model, Locale locale) {
        String errorMessage = getErrorCode(errorCode, locale);
        model.addAttribute("errorMessage", errorMessage);
        return "registration/bad-user";
    }

    private String getErrorCode(String errorCode, Locale locale) {
        return switch (errorCode) {
            case "expired_token" -> messages.getMessage("auth.message.expired", null, locale);
            case "invalid_token" -> messages.getMessage("auth.message.invalidToken", null, locale);
            default -> "An error occurred.";
        };
    }

}
