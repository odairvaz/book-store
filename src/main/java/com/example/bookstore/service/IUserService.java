package com.example.bookstore.service;

import com.example.bookstore.persistense.model.User;
import com.example.bookstore.web.dto.UserDto;

public interface IUserService {

    User registerNewUserAccount(UserDto accountDto);
}
