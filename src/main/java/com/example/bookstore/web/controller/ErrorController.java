package com.example.bookstore.web.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @RequestMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("message", "You don't have permission to access this page.");
        return "error/access-denied";
    }

}
