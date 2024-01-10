package com.example.bookstore.web.controller;

import com.example.bookstore.persistense.model.PasswordResetToken;
import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.model.VerificationToken;
import com.example.bookstore.registration.OnRegistrationCompleteEvent;
import com.example.bookstore.security.TokenWrapper;
import com.example.bookstore.service.IUserService;
import com.example.bookstore.web.dto.PasswordDto;
import com.example.bookstore.web.dto.UserDto;
import com.example.bookstore.web.error.UserAlreadyExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class RegistrationControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MessageSource messageSource;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TokenWrapper tokenWrapper;

    @InjectMocks
    private RegistrationController registrationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(registrationController).build();
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
                        .param("password", "Aczd139!")
                        .param("matchingPassword", "Aczd139!")
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
                        .param("password", "Aczd139!")
                        .param("matchingPassword", "Aczd139!")
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
                        .param("password", "Aczd139!")
                        .param("matchingPassword", "Aczd139!")
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
                        .param("password", "Aczd139!")
                        .param("matchingPassword", "Aczd139!")
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

        assertEquals("redirect:/api/bad-user?lang=en&token=invalid-token&error=invalid_token", result);
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
        assertEquals("redirect:/api/bad-user?lang=en&token=expired-token&error=expired_token", result);
    }

    @Test
    void givenSuccessRegisterPageRequest_whenSuccessRegister_thenReturnSuccessRegisterView() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Success message");

        mockMvc.perform(get("/api/success-register"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registration/success-register"))
                .andExpect(model().attributeExists("successMessage"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"expired_token", "invalid_token", "default"})
    void givenBadUserPageRequest_whenErrorParam_thenReturnBadUserView(String error) throws Exception {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error Message");

        mockMvc.perform(get("/api/bad-user").param("error", error))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registration/bad-user"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void givenSuccessRegenerateNewTokenPageRequest_whenResendRegistrationToken_thenReturnSuccessRegenerateTokenView() throws Exception {
        mockMvc.perform(get("/api/success-regenerate-token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registration/success-regenerate-token"));
    }

    @Test
    void givenExpiredToken_whenResendRegistrationToken_thenRedirectToSuccessPage() {
        String expiredToken = "expired-token";
        VerificationToken newToken = new VerificationToken();
        User user = new User();
        user.setEmail("user@user.com");
        newToken.setToken("new-token");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName("en");
        when(userService.generateNewVerificationToken(expiredToken)).thenReturn(newToken);
        when(userService.getUser(newToken.getToken())).thenReturn(user);

        String result = registrationController.resendRegistrationToken(expiredToken, request);

        verify(userService).generateNewVerificationToken(expiredToken);
        verify(userService).getUser(newToken.getToken());
        verify(eventPublisher).publishEvent(any(OnRegistrationCompleteEvent.class));

        assertEquals("redirect:/api/success-regenerate-token?lang=en", result);
    }

    @Test
    void givenInvalidToken_whenResendRegistrationToken_thenRedirectToBadUserPage() {
        String invalidToken = "invalid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName("en");
        when(userService.generateNewVerificationToken(invalidToken)).thenReturn(null);

        String result = registrationController.resendRegistrationToken(invalidToken, request);

        assertEquals("redirect:/api/bad-user?lang=en&token=invalid-token&error=invalid_token", result);
    }

    @Test
    void givenErrorParameter_whenShowForgetPasswordPage_thenErrorMessageAddedToModel() throws Exception {
        String errorParam = "invalid-email";

        mockMvc.perform(get("/api/forget-password").param("error", errorParam))
                .andExpect(status().isOk());
    }

    @Test
    void givenNoErrorParameter_whenShowForgetPasswordPage_thenNoErrorMessageAddedToModel() throws Exception {
        mockMvc.perform(get("/api/forget-password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    void givenValidUserEmail_whenResetPassword_thenRedirectToLogin() throws Exception {
        String validEmail = "valid.user@bookstore.com";
        User mockUser = new User();

        when(userService.findUserByEmail(validEmail)).thenReturn(mockUser);

        mockMvc.perform(post("/api/reset-password")
                        .param("email", validEmail))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login?lang=" + Locale.getDefault().getLanguage()));
    }

    @Test
    void givenInvalidUserEmail_whenResetPassword_thenRedirectToForgetPasswordWithError() throws Exception {
        String invalidEmail = "invalid.user@bookstore.com";

        when(userService.findUserByEmail(invalidEmail)).thenReturn(null);

        mockMvc.perform(post("/api/reset-password")
                        .param("email", invalidEmail))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/forget-password?lang=" + Locale.getDefault().getLanguage() + "&error=" + "invalid-email"));
    }

    @Test
    void givenMailAuthenticationException_whenResetPassword_thenRedirectToEmailError() throws Exception {
        String validEmail = "valid.user@bookstore.com";

        when(userService.findUserByEmail(validEmail)).thenReturn(new User());
        doThrow(new MailAuthenticationException("Mail Authentication Exception"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        mockMvc.perform(post("/api/reset-password")
                        .param("email", validEmail))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emailError.html?lang=" + Locale.getDefault().getLanguage()));
    }

    @Test
    void givenValidTokenAndNonExpiredToken_whenShowChangePassword_thenRedirectToLogin() throws Exception {
        String validToken = "valid-token";
        when(tokenWrapper.isTokenFound()).thenReturn(true);
        when(tokenWrapper.isTokenExpired()).thenReturn(false);

        mockMvc.perform(get("/api/update-password").param("token", validToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login.html?lang=" + Locale.getDefault().getLanguage()));
    }

    @Test
    void givenValidTokenAndValidPasswordDto_whenSavePassword_thenSuccessfully() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        PasswordResetToken validPasswordResetToken = new PasswordResetToken();
        LocalDate nonExpiredDate = LocalDate.now().plusDays(7);
        validPasswordResetToken.setExpiryDate(Date.from(nonExpiredDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        String validToken = "valid-token";
        PasswordDto validPasswordDto = new PasswordDto();
        validPasswordDto.setPassword("P@ssword169");
        validPasswordDto.setMatchingPassword("P@ssword169");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getPasswordResetToken(validToken)).thenReturn(validPasswordResetToken);

        mockMvc.perform(post("/api/save-password")
                        .param("token", validToken)
                        .flashAttr("password", validPasswordDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login?lang=" + Locale.getDefault().getLanguage()));
    }

    @Test
    void givenValidTokenAndInvalidPasswordDto_whenSavePassword_thenViewWithErrors() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);

        String validToken = "valid-token";
        PasswordDto invalidPasswordDto = new PasswordDto();
        invalidPasswordDto.setPassword("password1");
        invalidPasswordDto.setMatchingPassword("password2");

        when(bindingResult.hasErrors()).thenReturn(true);

        mockMvc.perform(post("/api/save-password")
                        .param("token", validToken)
                        .flashAttr("password", invalidPasswordDto))
                .andExpect(status().isOk())
                .andExpect(view().name("password/update-password"))
                .andExpect(model().attributeHasFieldErrors("password", "password"));
    }

    @Test
    void givenInvalidToken_whenShowChangePassword_thenRedirectToLogin() throws Exception {
        String invalidToken = "invalid-token";
        when(tokenWrapper.isTokenFound()).thenReturn(false);

        mockMvc.perform(get("/api/update-password").param("token", invalidToken))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void givenExpiredToken_whenShowChangePassword_thenRedirectToLogin() throws Exception {
        String expiredToken = "expired-token";
        when(tokenWrapper.isTokenFound()).thenReturn(true);
        when(tokenWrapper.isTokenExpired()).thenReturn(true);

        mockMvc.perform(get("/api/update-password").param("token", expiredToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html?lang=" + Locale.getDefault().getLanguage()));

    }

    @Test
    void givenValidTokenAndNonExpiredToken_whenShowChangePassword_thenCorrectView() throws Exception {
        String validToken = "valid-token";

        PasswordResetToken validPasswordResetToken = new PasswordResetToken();
        LocalDate nonExpiredDate = LocalDate.now().plusDays(7);
        validPasswordResetToken.setExpiryDate(Date.from(nonExpiredDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));


        when(userService.getPasswordResetToken(validToken)).thenReturn(validPasswordResetToken);
        when(tokenWrapper.isTokenFound()).thenReturn(true);
        when(tokenWrapper.isTokenExpired()).thenReturn(false);

        mockMvc.perform(get("/api/update-password").param("token", validToken))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("password", "token"))
                .andExpect(view().name("password/update-password"));
    }

}
