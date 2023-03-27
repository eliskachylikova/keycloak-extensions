package cz.mendelu.pef.xchyliko.keycloak.extensions.emailNotifications;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@JBossLog
public class EmailNotificationsProvider implements EventListenerProvider {

    private final KeycloakSession session;

    public EmailNotificationsProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {

        if (event.getType().equals(EventType.LOGIN)) {
            UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());
            var currentIP = session.getContext().getRequestHeaders().getHeaderString("X-Forwarded-For");

            // first time login from this IP address
            if (currentIP != null && (user.getAttributes().get("lastLoginIP") == null || !user.getAttributes().get("lastLoginIP").contains(currentIP))) {
                log.warn("This is first time login from this IP: " + currentIP);
                log.info("Adding IP " + currentIP + " to list.");
                log.info("Sending notification e-mail.");
                sendNotificationEmail(session.getContext(), user, currentIP);

                if (user.getAttributes().get("lastLoginIP") == null)
                    // first login ever
                    user.setSingleAttribute("lastLoginIP", currentIP);
                else {
                    // first login only from current IP
                    var addresses = user.getAttributes().get("lastLoginIP");
                    addresses.add(currentIP);
                    user.setAttribute("lastLoginIP", addresses);
                }
            }

            log.info("List of used IPs: " + user.getAttributes().get("lastLoginIP"));
        }
    }

    private void sendNotificationEmail(KeycloakContext context, UserModel userModel, String currentIP) {

        Map<String, String> smtpConfig = context.getRealm().getSmtpConfig();
        if (smtpConfig == null || smtpConfig.isEmpty()) {
            return;
        }

        String location;
        try {
            location = LocationService.getLocationOfIp(currentIP);
        } catch (IOException e) {
            log.error("Unable to get location: " + e.getMessage());
            location = "unknown location";
        }

        EmailSenderProvider emailSenderProvider = session.getProvider(EmailSenderProvider.class);

        Locale locale = session.getContext().resolveLocale(userModel);
        ResourceBundle resourceBundle = ResourceBundle.getBundle("email_content", locale);

        String htmlBody = resourceBundle.getString("htmlBody").replace("${username}", userModel.getUsername()).replace("${currentIP}", currentIP).replace("${location}", location);
        String textBody = resourceBundle.getString("textBody").replace("${username}", userModel.getUsername()).replace("${currentIP}", currentIP).replace("${location}", location);

        try {
            if (userModel.getEmail() != null)
                emailSenderProvider.send(smtpConfig, userModel, resourceBundle.getString("subject"), textBody, htmlBody);
            else
                log.error("Failed to send email: User does not have their e-mail set.");
        } catch (EmailException e) {
            log.error("Failed to send email: " + e.getMessage());
        }
    }


    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        log.infof("onEvent adminEvent=%s type=%s resourceType=%s resourcePath=%s includeRepresentation=%s", event, event.getOperationType(), event.getResourceType(), event.getResourcePath(), includeRepresentation);
    }

    @Override
    public void close() {
        // log.infof("close");
    }
}