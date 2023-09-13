package com.example.bookstore.web.controller;


import com.example.bookstore.service.IUserService;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/user")
public class RegistrationController {

    public static final String REGISTRATION_PAGE = "registration";
    private final IUserService userService;

    public RegistrationController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return REGISTRATION_PAGE;
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return REGISTRATION_PAGE;
        }

        try {
            userService.registerNewUserAccount(userDto);
        } catch (UserAlreadyExistException uaeEx) {
            bindingResult.rejectValue("email", "error.user", "An account with the email already exists.");
            return REGISTRATION_PAGE;
        }
        return "successRegister";
    }
}
