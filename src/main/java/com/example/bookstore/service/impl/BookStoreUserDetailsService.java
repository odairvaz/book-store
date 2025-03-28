package com.example.bookstore.service.impl;

import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.repository.IUserRepository;
import com.example.bookstore.security.core.userdetails.BookStoreUserDetail;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;


@Service
public class BookStoreUserDetailsService implements UserDetailsService {


    private final IUserRepository userRepository;

    public BookStoreUserDetailsService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new BookStoreUserDetail(user.getFirstName(), user.getEmail(), user.getPassword(), authorities);
    }

}
