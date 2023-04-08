package cz.mendelu.pef.xchyliko.keycloak.extensions.loginSessionListener;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@JBossLog
public class LoginSessionListenerProviderFactory implements EventListenerProviderFactory {

    private static final String ID = "session-info-listener";

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new LoginSessionListenerProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return ID;
    }
}
