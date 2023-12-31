package com.example.bookstore.service;

import com.example.bookstore.persistense.model.PasswordResetToken;
import com.example.bookstore.persistense.model.Role;
import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.model.VerificationToken;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;

import java.util.List;

public interface IUserService {

    User registerNewUserAccount(UserDto accountDto) throws UserAlreadyExistException;

    void createUser(String firstName, String lastName, String email, String password, List<Role> roles);

    void createVerificationToken(final User user, final String token);

    VerificationToken generateNewVerificationToken(String token);

    void createPasswordResetTokenForUser(User user, String token);

    PasswordResetToken getPasswordResetToken(String token);

    void changeUserPassword(User user, String password);

    boolean checkIfValidOldPassword(User user, String oldPassword);

    User getUser(String verificationToken);

    void saveRegisteredUser(User user);

    VerificationToken getVerificationToken(String verificationToken);

    User findUserByEmail(String email);

}
