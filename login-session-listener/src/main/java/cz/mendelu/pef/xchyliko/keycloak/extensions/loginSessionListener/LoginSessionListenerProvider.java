package cz.mendelu.pef.xchyliko.keycloak.extensions.loginSessionListener;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@JBossLog
public class LoginSessionListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;

    public LoginSessionListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {

        if (event.getType().equals(EventType.LOGIN)) {

            UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());
            var currentIP = session.getContext().getRequestHeaders().getHeaderString("X-Forwarded-For");

            if (currentIP == null)
                return;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String time = formatter.format(LocalTime.now());

            String sessionInfo = "IP address: " + currentIP + ", Date: " + LocalDate.now() + ", Time: " + time;

            if (user.getAttributes().get("sessionInfo") == null) {
                user.setSingleAttribute("sessionInfo", sessionInfo);
            } else {
                var infoList = user.getAttributes().get("sessionInfo");
                infoList.add(sessionInfo);
                user.setAttribute("sessionInfo", infoList);
            }
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

    }

    @Override
    public void close() {

    }
}
