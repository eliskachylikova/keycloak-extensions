package cz.mendelu.pef.xchyliko.keycloak.extensions.emailNotifications;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
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
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@JBossLog
public class EmailNotificationsProvider implements EventListenerProvider {

    private final KeycloakSession session;
    private final Config.Scope config;

    public EmailNotificationsProvider(KeycloakSession session, Config.Scope config) {
        this.session = session;
        this.config = config;
    }

    @Override
    public void onEvent(Event event) {

        if (!event.getType().equals(EventType.LOGIN))
            return;

        UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());
        var currentIP = session.getContext().getHttpRequest().getHttpHeaders().getHeaderString("X-Forwarded-For");
        var savedAddresses = user.getAttributes().get("loginIPAddresses");

        // first time login from this IP address
        if (currentIP != null && (savedAddresses == null || !savedAddresses.contains(currentIP))) {
            log.info("This is first time login from this IP: " + currentIP);
            log.info("Adding IP " + currentIP + " to list.");
            log.info("Sending notification e-mail.");
            sendNotificationEmail(session.getContext(), user, currentIP, config.get("COUNTRY_URL"), config.get("ACCOUNT_ID"), config.get("LICENSE_KEY"), config.get("SUBJECT_PREFIX"));

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

    private void sendNotificationEmail(KeycloakContext context, UserModel userModel, String currentIP, String countryUrl, String accountID, String licenseKey, String subjectPrefix) {

        // get smtpConfig that is needed to send the e-mail properly
        Map<String, String> smtpConfig = context.getRealm().getSmtpConfig();
        if (smtpConfig == null || smtpConfig.isEmpty()) {
            return;
        }

	// get local langauge of user
        Locale locale = session.getContext().resolveLocale(userModel);

        // get location
        String location;
        try {
            location = LocationService.getLocationOfIp(new URL(countryUrl + currentIP), accountID, licenseKey, locale.getLanguage());
        } catch (IOException e) {
            log.error("Unable to get location of IP address: " + e.getMessage());
            location = "unknown location";
        }

        // get user agent e.g. Firefox
        var userAgent = context.getHttpRequest().getHttpHeaders().getHeaderString("User-Agent");

        // determine which language use in e-mail if there are more to choose
        ResourceBundle resourceBundle = ResourceBundle.getBundle("email_content", locale);

        // complete e-mail content
        String subject = resourceBundle.getString("subject")
                .replace("${subjectPrefix}", subjectPrefix);

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
                emailSenderProvider.send(smtpConfig, userModel, subject, textBody, htmlBody);
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
