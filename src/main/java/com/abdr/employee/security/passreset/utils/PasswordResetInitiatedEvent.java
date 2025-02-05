package com.abdr.employee.security.passreset.utils;

import com.abdr.employee.security.entities.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PasswordResetInitiatedEvent extends ApplicationEvent {
    private final User user;

    public PasswordResetInitiatedEvent(User user) {
        super(user);
        this.user = user;
    }
}
