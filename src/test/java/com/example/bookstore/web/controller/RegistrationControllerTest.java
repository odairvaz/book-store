package com.example.bookstore.web.controller;

import com.example.bookstore.persistense.model.User;
import com.example.bookstore.registration.OnRegistrationCompleteEvent;
import com.example.bookstore.service.IUserService;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.ModelResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;


class RegistrationControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private RegistrationController registrationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(registrationController).build();
    }

    @Test
    void givenRegistrationPageRequest_whenShowRegistrationForm_thenReturnRegistrationView() throws Exception {
        mockMvc.perform(get("/api/registration"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registration/registration"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void givenValidUserData_whenRegisterUserAccount_thenReturnSuccessRegisterView() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerNewUserAccount(any(UserDto.class))).thenReturn(new User());

        mockMvc.perform(post("/api/registration")
                        .param("firstName", "User")
                        .param("lastName", "User")
                        .param("password", "123")
                        .param("matchingPassword", "123")
                        .param("email", "user@company.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration/success-register"));

        verify(eventPublisher, times(1)).publishEvent(any(OnRegistrationCompleteEvent.class));
    }

    @Test
    void givenDifferentPasswordAndMatchingPassword_whenRegisterUserAccount_thenReturnRegisterView() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerNewUserAccount(any(UserDto.class))).thenReturn(new User());

        mockMvc.perform(post("/api/registration")
                        .param("firstName", "User")
                        .param("lastName", "User")
                        .param("password", "123")
                        .param("matchingPassword", "321")
                        .param("email", "user@company.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration/registration"));

        verifyNoInteractions(eventPublisher);
        verifyNoInteractions(userService);
    }

    /*@Test
    void givenInvalidEmailFormat_whenRegisterUserAccount_thenReturnRegistrationViewWithErrors() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);

        mockMvc.perform(post("/api/registration")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("password", "password123")
                        .param("matchingPassword", "password123")
                        .param("email", "invalid_mail"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeHasFieldErrors("user", "email"))
                .andExpect(MockMvcResultMatchers.view().name("registration/registration"));

        verifyNoInteractions(userService);
    }*/

    @Test
    void givenUserAlreadyExists_whenRegisterUserAccount_thenReturnRegistrationViewWithErrorMessage() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);

        when(userService.registerNewUserAccount(any(UserDto.class)))
                .thenThrow(new UserAlreadyExistException("User with this email already exists"));

        mockMvc.perform(post("/api/registration")
                        .param("firstName", "User")
                        .param("lastName", "User")
                        .param("password", "123")
                        .param("matchingPassword", "123")
                        .param("email", "user@company.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration/registration"))
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "error.user"));

        verifyNoInteractions(eventPublisher);
    }



}