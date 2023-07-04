package com.marcus.securebusiness.listener;

import com.marcus.securebusiness.event.NewUserEvent;
import com.marcus.securebusiness.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewUserEventListener {
    private final EventService eventService;
    private final HttpServletRequest request;

    @EventListener
    public void onNewUserEvent(NewUserEvent event) {
        log.info("NewUserEvent is fired");
        eventService.addUserEvent(event.getEmail(), event.getType(), "Device", "IP Address");
    }
}
