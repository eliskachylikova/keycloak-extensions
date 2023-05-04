import cz.mendelu.pef.xchyliko.keycloak.extensions.loginSessionListener.LocationService;
import cz.mendelu.pef.xchyliko.keycloak.extensions.loginSessionListener.LoginSessionListenerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.HttpHeaders;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginSessionTest {

    @Test
    void onEvent_login() {
        // mock classes
        Event mockEvent = mock(Event.class);
        KeycloakSession mockSession = mock(KeycloakSession.class);
        UserModel mockUser = mock(UserModel.class);
        KeycloakContext mockContext = mock(KeycloakContext.class);
        RealmModel mockRealm = mock(RealmModel.class);
        UserProvider mockProvider = mock(UserProvider.class);
        HttpHeaders mockHeaders = mock(HttpHeaders.class);

        // mock methods
        when(mockEvent.getType()).thenReturn(EventType.LOGIN);
        when(mockSession.getContext()).thenReturn(mockContext);
        when(mockContext.getRealm()).thenReturn(mockRealm);
        when(mockSession.users()).thenReturn(mockProvider);
        when(mockSession.users().getUserById(mockRealm, mockUser.getId())).thenReturn(mockUser);

        when(mockContext.getRequestHeaders()).thenReturn(mockHeaders);
        when(mockHeaders.getHeaderString("X-Forwarded-For")).thenReturn("1.2.3.4");
        when(mockHeaders.getHeaderString("User-Agent")).thenReturn("Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/112.0");

        MockedStatic<LocationService> locationSevice = Mockito.mockStatic(LocationService.class);
        locationSevice.when(() -> LocationService.getLocationOfIp(new URL(LocationService.COUNTRY_URL + "1.2.3.4")))
                .thenReturn("Australia, Oceania");

        // create and mock sample user attributes
        Map<String, List<String>> attributes = new HashMap<>();
        List<String> sessionInfo = new ArrayList<>();
        sessionInfo.add("example");
        attributes.put("sessionInfo", sessionInfo);
        when(mockUser.getAttributes()).thenReturn(attributes);

        // instance the provider and call the tested method
        LoginSessionListenerProvider provider = new LoginSessionListenerProvider(mockSession);
        provider.onEvent(mockEvent);

        // create updated attributes
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = formatter.format(LocalTime.now());
        var updatedSessionInfo = List.of("example", "IP address: 1.2.3.4, Date: " + LocalDate.now() + ", Time: " + time + " " + ZoneId.systemDefault() + ", Agent: Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/112.0, Location: Australia, Oceania");

        // verify if the parameter has been set and with correct data
        verify(mockUser, times(1)).setAttribute("sessionInfo", updatedSessionInfo);
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
        LoginSessionListenerProvider provider = new LoginSessionListenerProvider(mockSession);
        provider.onEvent(mockEvent);

        // create updated attributes
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = formatter.format(LocalTime.now());
        var updatedSessionInfo = List.of("example", "IP address: 1.2.3.4, Date: " + LocalDate.now() + ", Time: " + time + ", Agent: Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/112.0, Location: Australia, Oceania");

        // the method should not be called because the event was not a LOGIN
        verify(mockUser, times(0)).setAttribute("sessionInfo", updatedSessionInfo);
    }

}
