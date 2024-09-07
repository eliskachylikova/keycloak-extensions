package cz.mendelu.pef.xchyliko.keycloak.extensions.emailNotifications;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@JBossLog
public class EmailNotificationsProviderFactory implements EventListenerProviderFactory {

    private static final String ID = "login-new-ip-email-notifications";
    private Config.Scope config;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new EmailNotificationsProvider(session, config);
    }

    @Override
    public void init(Config.Scope config) {
        log.infof("init config={}", config);
        this.config = config;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        log.infof("postInit factory={}", factory);
    }

    @Override
    public void close() {
        log.infof("close");
    }

    @Override
    public String getId() {
        return ID;
    }
}
