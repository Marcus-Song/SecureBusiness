package com.marcus.securebusiness.service.implementation;

import com.marcus.securebusiness.enumeration.EventType;
import com.marcus.securebusiness.model.UserEvent;
import com.marcus.securebusiness.repository.EventRepository;
import com.marcus.securebusiness.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    @Override
    public Collection<UserEvent> getEventByUserId(Long userId) {
        return eventRepository.getEventByUserId(userId);
    }

    @Override
    public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {
        eventRepository.addUserEvent(email, eventType, device, ipAddress);
    }

    @Override
    public void addUserEvent(Long userId, EventType eventType, String device, String ipAddress) {

    }
}
