package com.example.bookstore.web.controller;


import com.example.bookstore.persistense.model.User;
import com.example.bookstore.security.core.userdetails.BookStoreUserDetail;
import com.example.bookstore.service.IUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/api/my-account")
public class AccountController {

    private final IUserService userService;

    public AccountController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String home(Model model,@AuthenticationPrincipal BookStoreUserDetail userDetails) {
        User user = userService.findUserByEmail(userDetails.getEmail());
        model.addAttribute("user", user);
        return "/account/account";
    }

    @GetMapping("/info/{id}")
    public String editUserInfo(Model model, @PathVariable Long id) {
        User user = userService.findUserById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        return "account/info";
    }

    @PostMapping("/update/{id}")
    public String saveUser(@PathVariable Long id, User user) {
        Optional<User> optionalUser = userService.findUserById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            userService.saveRegisteredUser(existingUser);
            return "redirect:/api/my-account";
        } else {
            throw new IllegalArgumentException("Invalid user Id:" + id);
        }
    }
}
