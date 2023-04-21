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

        if (!event.getType().equals(EventType.LOGIN))
            return;

        UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());
        // todo pouze pro debug ucely, zmenit pred nasazenim
        var currentIP = session.getContext().getRequestHeaders().getHeaderString("X-Forwarded-For") != null ? session.getContext().getRequestHeaders().getHeaderString("X-Forwarded-For") : session.getContext().getConnection().getRemoteAddr();
        var savedAddresses = user.getAttributes().get("loginIPAddresses");

        // first time login from this IP address
        if (currentIP != null && (savedAddresses == null || !savedAddresses.contains(currentIP))) {
            log.warn("This is first time login from this IP: " + currentIP);
            log.info("Adding IP " + currentIP + " to list.");
            log.info("Sending notification e-mail.");
            sendNotificationEmail(session.getContext(), user, currentIP);

            if (user.getAttributes().get("loginIPAddresses") == null)
                // first login ever
                user.setSingleAttribute("loginIPAddresses", currentIP);
            else {
                // first login only from current IP
                var addresses = user.getAttributes().get("loginIPAddresses");
                addresses.add(currentIP);
                user.setAttribute("loginIPAddresses", addresses);
            }
        }

        log.info("List of used IPs: " + user.getAttributes().get("loginIPAddresses"));
    }

    private void sendNotificationEmail(KeycloakContext context, UserModel userModel, String currentIP) {

        // get smtpConfig that is needed to send the e-mail properly
        Map<String, String> smtpConfig = context.getRealm().getSmtpConfig();
        if (smtpConfig == null || smtpConfig.isEmpty()) {
            return;
        }

        // get location
        String location;
        try {
            location = LocationService.getLocationOfIp(currentIP);
        } catch (IOException e) {
            log.error("Unable to get location of IP address: " + e.getMessage());
            location = "unknown location";
        }

        // get user agent e.g. Firefox
        var userAgent = context.getRequestHeaders().getHeaderString("User-Agent");

        // determine which language use in e-mail if there are more to choose
        Locale locale = session.getContext().resolveLocale(userModel);
        ResourceBundle resourceBundle = ResourceBundle.getBundle("email_content", locale);

        // complete e-mail content
        String htmlBody = resourceBundle.getString("htmlBody")
                .replace("${username}", userModel.getUsername())
                .replace("${currentIP}", currentIP)
                .replace("${location}", location)
                .replace("${userAgent}", userAgent);

        String textBody = resourceBundle.getString("textBody")
                .replace("${username}", userModel.getUsername())
                .replace("${currentIP}", currentIP)
                .replace("${location}", location)
                .replace("${userAgent}", userAgent);

        // try to send e-mail
        EmailSenderProvider emailSenderProvider = session.getProvider(EmailSenderProvider.class);

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
