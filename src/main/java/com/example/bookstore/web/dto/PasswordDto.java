package com.example.bookstore.web.dto;

import com.example.bookstore.validation.PasswordMatches;
import com.example.bookstore.validation.ValidPassword;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@PasswordMatches
public class PasswordDto {

    @ValidPassword
    private String password;

    private String matchingPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatchingPassword() {
        return matchingPassword;
    }

    public void setMatchingPassword(String matchingPassword) {
        this.matchingPassword = matchingPassword;
    }
}
