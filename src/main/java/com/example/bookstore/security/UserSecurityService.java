package com.example.bookstore.security;


import com.example.bookstore.persistense.model.PasswordResetToken;
import com.example.bookstore.persistense.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class UserSecurityService {

    private final PasswordResetTokenRepository passwordTokenRepository;

    public UserSecurityService(PasswordResetTokenRepository passwordTokenRepository) {
        this.passwordTokenRepository = passwordTokenRepository;
    }

    public boolean validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);

        return isTokenFound(passToken) && !isTokenExpired(passToken);
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }
}
