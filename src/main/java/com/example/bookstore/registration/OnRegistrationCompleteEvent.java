package com.example.bookstore.registration;

import com.example.bookstore.persistense.model.User;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private final String appUrl;
    private final Locale locale;
    private final User user;
    private final String existingToken;

    public OnRegistrationCompleteEvent(User user, Locale locale, String appUrl, String existingToken) {
        super(user);
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
        this.existingToken = existingToken;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public User getUser() {
        return user;
    }

    public String getExistingToken() {
        return existingToken;
    }
}
