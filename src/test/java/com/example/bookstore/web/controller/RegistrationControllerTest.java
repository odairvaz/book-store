package com.example.bookstore.web.controller;

import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.model.VerificationToken;
import com.example.bookstore.registration.OnRegistrationCompleteEvent;
import com.example.bookstore.service.IUserService;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
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
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Success message");

        mockMvc.perform(post("/api/registration")
                        .param("firstName", "User")
                        .param("lastName", "User")
                        .param("password", "123")
                        .param("matchingPassword", "123")
                        .param("email", "user@company.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("successMessage"))
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
        when(bindingResult.hasErrors()).thenReturn(false);

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

    @Test
    void givenUser_whenPublishEvent_thenThrowRuntimeException() throws Exception {
        when(userService.registerNewUserAccount(any(UserDto.class))).thenReturn(new User());

        doThrow(new RuntimeException("Unable to register user"))
                .when(eventPublisher)
                .publishEvent(any(OnRegistrationCompleteEvent.class));

        mockMvc.perform(post("/api/registration")
                        .param("firstName", "User")
                        .param("lastName", "User")
                        .param("password", "123")
                        .param("matchingPassword", "123")
                        .param("email", "user@company.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration/error-registration"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(eventPublisher, times(1)).publishEvent(any(OnRegistrationCompleteEvent.class));
    }

    @Test
    void givenValidToken_whenConfirmRegistration_thenUserIsEnabledAndRedirectToSuccessPage() {
        String token = "valid-token";
        VerificationToken verificationToken = new VerificationToken();
        User user = new User();
        user.setEnabled(false);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(new Date(System.currentTimeMillis() + 86400_000));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName(Locale.ENGLISH.getLanguage());

        when(userService.getVerificationToken(token)).thenReturn(verificationToken);
        String result = registrationController.confirmRegistration(request, token);

        verify(userService).getVerificationToken(token);
        verify(userService).saveRegisteredUser(user);

        assertTrue(user.isEnabled());
        assertEquals("redirect:/api/success-register?lang=en", result);
    }

    @Test
    void givenInvalidToken_whenConfirmRegistration_thenRedirectToBadUserPage() {
        String token = "invalid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName(Locale.ENGLISH.getLanguage());

        when(userService.getVerificationToken(token)).thenReturn(null);
        String result = registrationController.confirmRegistration(request, token);

        assertEquals("redirect:/api/bad-user?lang=en&error=invalid_token", result);
    }

    @Test
    void givenExpiredToken_whenConfirmRegistration_thenRedirectToBadUserPage() {
        String token = "expired-token";
        VerificationToken verificationToken = new VerificationToken();
        User user = new User();
        user.setEnabled(false);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(new Date(System.currentTimeMillis() - 86400_000));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName(Locale.ENGLISH.getLanguage());

        when(userService.getVerificationToken(token)).thenReturn(verificationToken);
        String result = registrationController.confirmRegistration(request, token);

        verify(userService).getVerificationToken(token);
        assertFalse(user.isEnabled());
        assertEquals("redirect:/api/bad-user?lang=en&error=expired_token", result);
    }

    @Test
    void givenSuccessRegisterPageRequest_whenSuccessRegister_thenReturnSuccessRegisterView() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Success message");

        mockMvc.perform(get("/api/success-register"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registration/success-register"))
                .andExpect(model().attributeExists("successMessage"));
    }

    @Test
    void givenBadUserPageRequest_whenErrorExpiredToken_thenReturnBadUserView() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error Message");

        mockMvc.perform(get("/api/bad-user")
                        .param("error", "expired_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registration/bad-user"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void givenBadUserPageRequest_whenErrorInvalidToken_thenReturnBadUserView() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error Message");

        mockMvc.perform(get("/api/bad-user")
                        .param("error", "invalid_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registration/bad-user"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void givenBadUserPageRequest_whenErrorDefault_thenReturnBadUserView() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error Message");

        mockMvc.perform(get("/api/bad-user")
                        .param("error", "default"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registration/bad-user"))
                .andExpect(model().attributeExists("errorMessage"));
    }

}
