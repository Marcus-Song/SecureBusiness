package com.marcus.securebusiness.query;

public class EventQuery {
    public static final String SELECT_EVENTS_BY_USER_ID_QUERY =
            "SELECT ue.device, ue.ip_address, ue.created_at, e.type, e.description FROM UserEvents ue JOIN Events e ON e.id = ue.event_id JOIN Users u ON u.id = ue.user_id WHERE ue.user_id = :id ORDER BY ue.created_at DESC LIMIT 10";
    public static final String INSERT_EVENT_BY_USER_EMAIL_QUERY =
            "INSERT INTO UserEvents (user_id, event_id, device, ip_address) VALUES ((SELECT id FROM Users WHERE email = :email), (SELECT id FROM Events WHERE type = :type), :device, :ipAddress)";

}
