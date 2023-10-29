package com.example.bookstore.security;

import com.example.bookstore.persistense.model.PasswordResetToken;
import com.example.bookstore.persistense.model.VerificationToken;

import java.util.Calendar;


public class TokenWrapper implements Token {
    private PasswordResetToken passwordResetToken;
    private VerificationToken verificationToken;

    public TokenWrapper(PasswordResetToken passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public TokenWrapper(VerificationToken verificationToken) {
        this.verificationToken = verificationToken;
    }

    @Override
    public boolean isTokenFound() {
        return passwordResetToken != null || verificationToken != null;
    }

    @Override
    public boolean isTokenExpired() {
        final Calendar cal = Calendar.getInstance();

        if (passwordResetToken != null) {
            return passwordResetToken.getExpiryDate().before(cal.getTime());
        } else if (verificationToken != null) {
            return verificationToken.getExpiryDate().before(cal.getTime());
        }
        return false;
    }
}