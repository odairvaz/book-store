package com.example.bookstore.service.impl;

import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.model.VerificationToken;
import com.example.bookstore.persistense.repository.IRoleRepository;
import com.example.bookstore.persistense.repository.IUserRepository;
import com.example.bookstore.persistense.repository.VerificationTokenRepository;
import com.example.bookstore.service.IUserService;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@Transactional
public class IUserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final VerificationTokenRepository tokenRepository;

    public IUserServiceImpl(IUserRepository userRepository, IRoleRepository roleRepository, VerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public User registerNewUserAccount(UserDto userDto) {
        if (emailExists(userDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: " + userDto.getEmail());
        }
        final User user = new User();

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPassword(userDto.getPassword());
        user.setEmail(userDto.getEmail());
        user.setRoles(Collections.singletonList(roleRepository.findByName("ROLE_USER")));
        return userRepository.save(user);
    }

    private boolean emailExists(final String email) {
        return userRepository.findByEmail(email) != null;
    }

    @Override
    public void createVerificationToken(final User user, final String token) {
        final VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }

    @Override
    public VerificationToken generateNewVerificationToken(String existingVerificationToken) {
        VerificationToken vToken = tokenRepository.findByToken(existingVerificationToken);
        if (vToken == null) {
            return null;
        }
        vToken.updateToken(UUID.randomUUID().toString());
        vToken = tokenRepository.save(vToken);
        return vToken;
    }

    @Override
    public User getUser(String verificationToken) {
        return tokenRepository.findByToken(verificationToken).getUser();
    }

    @Override
    public void saveRegisteredUser(User user) {
        userRepository.save(user);
    }

    @Override
    public VerificationToken getVerificationToken(String verificationToken) {
        return tokenRepository.findByToken(verificationToken);
    }
}
