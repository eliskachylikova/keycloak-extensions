package cz.mendelu.pef.xchyliko.keycloak.extensions.loginSessionListener;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@JBossLog
public class LoginSessionListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;

    public LoginSessionListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    private final int MAX_SESSIONS = 20;

    @Override
    public void onEvent(Event event) {

        if (!event.getType().equals(EventType.LOGIN))
            return;

        UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());
        var currentIP = session.getContext().getHttpRequest().getHttpHeaders().getHeaderString("X-Forwarded-For");

        if (currentIP == null)
            return;

        var userAgent = context.getHttpRequest().getHttpHeaders().getHeaderString("User-Agent");

        String location;
        try {
            location = LocationService.getLocationOfIp(new URL(LocationService.COUNTRY_URL + currentIP));
        } catch (IOException e) {
            log.error("Unable to get location of IP address: " + e.getMessage());
            location = "unknown location";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = formatter.format(LocalTime.now());

        String sessionInfo = "IP address: " + currentIP + ", Date: " + LocalDate.now() + ", Time: " + time + " " + ZoneId.systemDefault() + ", Agent: " + userAgent + ", Location: " + location;

        if (user.getAttributes().get("sessionInfo") == null) {
            user.setSingleAttribute("sessionInfo", sessionInfo);
        } else {
            var savedSessions = user.getAttributes().get("sessionInfo");
            savedSessions.add(sessionInfo);
            user.setAttribute("sessionInfo", savedSessions);
        }

        deleteOldSessions(user);
    }

    private void deleteOldSessions(UserModel user) {
        var savedSessions = user.getAttributes().get("sessionInfo");
        if (savedSessions.size() > MAX_SESSIONS) {
            var howManyRemove = savedSessions.size() - MAX_SESSIONS;
            savedSessions.subList(0, howManyRemove).clear();
            user.setAttribute("sessionInfo", savedSessions);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {}

    @Override
    public void close() {}
}
