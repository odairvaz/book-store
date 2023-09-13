package com.example.bookstore.validation;

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
        UserDto user = (UserDto) obj;
        boolean passwordsMatch = user.getPassword().equals(user.getMatchingPassword());

        if (!passwordsMatch) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("password")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
