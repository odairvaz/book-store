package com.example.bookstore.service.impl;

import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.repository.IRoleRepository;
import com.example.bookstore.persistense.repository.IUserRepository;
import com.example.bookstore.service.IUserService;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Transactional
public class IUserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;

    public IUserServiceImpl(IUserRepository userRepository, IRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
}
