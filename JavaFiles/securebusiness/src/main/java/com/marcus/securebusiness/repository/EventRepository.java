package com.marcus.securebusiness.repository;

import com.marcus.securebusiness.enumeration.EventType;
import com.marcus.securebusiness.model.UserEvent;

import java.util.Collection;

public interface EventRepository {
    Collection<UserEvent> getEventByUserId(Long userId);
    void addUserEvent(String email, EventType eventType, String device, String ipAddress);
    void addUserEvent(Long userId, EventType eventType, String device, String ipAddress);
}
