import cz.mendelu.pef.xchyliko.keycloak.extensions.emailNotifications.EmailNotificationsProvider;
import cz.mendelu.pef.xchyliko.keycloak.extensions.emailNotifications.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailNotificationsTest {

    @Test
    void onEvent_success() throws EmailException {
        // mock classes
        Event mockEvent = mock(Event.class);
        KeycloakSession mockSession = mock(KeycloakSession.class);
        UserModel mockUser = mock(UserModel.class);
        KeycloakContext mockContext = mock(KeycloakContext.class);
        RealmModel mockRealm = mock(RealmModel.class);
        UserProvider mockProvider = mock(UserProvider.class);
        HttpHeaders mockHeaders = mock(HttpHeaders.class);
        EmailSenderProvider mockEmailProvider = mock(EmailSenderProvider.class);

        // mock methods
        when(mockEvent.getType()).thenReturn(EventType.LOGIN);
        when(mockSession.getContext()).thenReturn(mockContext);
        when(mockContext.getRealm()).thenReturn(mockRealm);
        when(mockSession.users()).thenReturn(mockProvider);
        when(mockSession.users().getUserById(mockRealm, mockUser.getId())).thenReturn(mockUser);

        when(mockContext.getRequestHeaders()).thenReturn(mockHeaders);
        when(mockHeaders.getHeaderString("X-Forwarded-For")).thenReturn("1.2.3.4");

        List<String> savedIpAdresses = new ArrayList<>();
        savedIpAdresses.add("5.6.7.8");
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("loginIPAddresses", savedIpAdresses);
        when(mockUser.getAttributes()).thenReturn(attributes);

        Map<String,String> smtpConfig = Map.of("example key", "example value");
        when(mockRealm.getSmtpConfig()).thenReturn(smtpConfig);
        when(mockContext.resolveLocale(mockUser)).thenReturn(new Locale("EN_US"));
        when(mockUser.getUsername()).thenReturn("username");

        MockedStatic<LocationService> locationSevice = Mockito.mockStatic(LocationService.class);
        locationSevice.when(() -> LocationService.getLocationOfIp(new URL(LocationService.COUNTRY_URL + "1.2.3.4")))
                .thenReturn("Australia, Oceania");

        when(mockHeaders.getHeaderString("User-Agent")).thenReturn("Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/112.0");
        when(mockUser.getEmail()).thenReturn("example@example.com");
        when(mockSession.getProvider(EmailSenderProvider.class)).thenReturn(mockEmailProvider);

        // instance the provider and call the tested method
        EmailNotificationsProvider provider = new EmailNotificationsProvider(mockSession);
        provider.onEvent(mockEvent);

        String emailBody = "Testing email with parameters: username, 1.2.3.4, Australia, Oceania, Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/112.0";

        // verify that the email was sent
        verify(mockRealm, times(1)).getSmtpConfig(); // get smtp config for email sending
        verify(mockHeaders, times(1)).getHeaderString("User-Agent");
        verify(mockEmailProvider, times(1)).send(smtpConfig, mockUser, "Testing email subject", emailBody, emailBody);
    }

    @Test
    void onEvent_error() throws EmailException {
        // mock classes
        Event mockEvent = mock(Event.class);
        KeycloakSession mockSession = mock(KeycloakSession.class);
        UserModel mockUser = mock(UserModel.class);
        KeycloakContext mockContext = mock(KeycloakContext.class);
        RealmModel mockRealm = mock(RealmModel.class);
        UserProvider mockProvider = mock(UserProvider.class);
        HttpHeaders mockHeaders = mock(HttpHeaders.class);
        EmailSenderProvider mockEmailProvider = mock(EmailSenderProvider.class);

        // mock methods
        when(mockEvent.getType()).thenReturn(EventType.LOGIN);
        when(mockSession.getContext()).thenReturn(mockContext);
        when(mockContext.getRealm()).thenReturn(mockRealm);
        when(mockSession.users()).thenReturn(mockProvider);
        when(mockSession.users().getUserById(mockRealm, mockUser.getId())).thenReturn(mockUser);
        when(mockContext.getRequestHeaders()).thenReturn(mockHeaders);
        when(mockHeaders.getHeaderString("X-Forwarded-For")).thenReturn("1.2.3.4");

        List<String> savedIpAdresses = new ArrayList<>();
        savedIpAdresses.add("5.6.7.8");
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("loginIPAddresses", savedIpAdresses);
        when(mockUser.getAttributes()).thenReturn(attributes);

        // instance the provider and call the tested method
        EmailNotificationsProvider provider = new EmailNotificationsProvider(mockSession);
        provider.onEvent(mockEvent);

        // verify that the email was not sent because there is no smtp config
        verify(mockRealm, times(1)).getSmtpConfig();
        // called zero times
        verify(mockHeaders, times(0)).getHeaderString("User-Agent");
        verify(mockEmailProvider, times(0)).send(Map.of("", ""), mockUser, "", "", "");
    }


    @Test
    void onEvent_notLogin() {
        // mock classes
        Event mockEvent = mock(Event.class);
        KeycloakSession mockSession = mock(KeycloakSession.class);
        UserModel mockUser = mock(UserModel.class);

        // mock method - event is something else than LOGIN
        when(mockEvent.getType()).thenReturn(EventType.LOGOUT);

        // instance the provider and call the tested method
        EmailNotificationsProvider provider = new EmailNotificationsProvider(mockSession);
        provider.onEvent(mockEvent);

        // the method should not be called because the event was not a LOGIN
        verify(mockUser, times(0)).setSingleAttribute("loginIPAddresses", "1.2.3.4");
        verify(mockUser, times(0)).setAttribute("loginIPAddresses", List.of("1.2.3.4"));
    }
}
