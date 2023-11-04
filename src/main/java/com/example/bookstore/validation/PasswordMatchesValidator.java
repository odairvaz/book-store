package com.example.bookstore.validation;

import com.example.bookstore.web.dto.PasswordDto;
import com.example.bookstore.web.dto.UserDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    private String message;

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        boolean passwordsMatch = switch (obj) {
            case UserDto userDto -> userDto.getPassword().equals(userDto.getMatchingPassword());
            case PasswordDto passwordDto -> passwordDto.getPassword().equals(passwordDto.getMatchingPassword());
            default -> false;
        };


        if (!passwordsMatch) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("password")
                    .addConstraintViolation();
        }
        return passwordsMatch;
    }

}
