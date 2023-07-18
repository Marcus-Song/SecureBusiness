package com.marcus.securebusiness.repository.implementation;

import com.marcus.securebusiness.enumeration.EventType;
import com.marcus.securebusiness.model.UserEvent;
import com.marcus.securebusiness.repository.EventRepository;
import com.marcus.securebusiness.rowMapper.UserEventRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.marcus.securebusiness.query.EventQuery.*;
import static java.util.Map.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventRepositoryImpl implements EventRepository {
    @Autowired
    private final NamedParameterJdbcTemplate jdbc;
    @Override
    public Collection<UserEvent> getEventByUserId(Long userId) {
        return jdbc.query(SELECT_EVENTS_BY_USER_ID_QUERY, of("id", userId), new UserEventRowMapper());
    }

    @Override
    public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {
        log.info("email: {}, eventType: {}, device: {}, ipAddress: {}",email, eventType, device, ipAddress);
        jdbc.update(INSERT_EVENT_BY_USER_EMAIL_QUERY, of("email", email, "type", eventType.toString(), "device", device, "ipAddress",ipAddress));
    }

    @Override
    public void addUserEvent(Long userId, EventType eventType, String device, String ipAddress) {

    }
}
