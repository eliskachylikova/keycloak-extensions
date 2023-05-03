import cz.mendelu.pef.xchyliko.keycloak.extensions.loginSessionListener.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.common.util.Base64;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Test
    void getLocationOfIp_success() throws IOException {
        String auth = "user:123";
        byte[] encodedAuth = Base64.encodeBytesToBytes(auth.getBytes());

        // example of what can geolocation API return
        String responseString = "{\"country\":{\"names\":{\"en\":\"United States\"}},\"continent\":{\"names\":{\"en\":\"North America\"}}}";
        InputStream inputStream = new ByteArrayInputStream(responseString.getBytes());

        // mock classes
        URL mockedUrl = mock(URL.class);
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);

        when(mockedUrl.openConnection()).thenReturn(mockedCon);
        when(mockedCon.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockedCon.getInputStream()).thenReturn(inputStream);

        LocationService.ACCOUNT_ID = "user";
        LocationService.LICENSE_KEY = "123";

        // call the method
        String result = LocationService.getLocationOfIp(mockedUrl);

        // assert and verify
        assertEquals("United States, North America", result);
        verify(mockedUrl, times(1)).openConnection();
        verify(mockedCon, times(1)).setRequestMethod("GET");
        verify(mockedCon, times(1)).setRequestProperty("Authorization", "Basic " + new String(encodedAuth));
        verify(mockedCon, times(1)).getResponseCode();
    }

    @Test
    void getLocationOfIp_error() throws IOException {

        // example of what can geolocation API return
        String responseString = "{\"code\":\"IP_ADDRESS_RESERVED\",\"error\":\"The IP address '127.0.0.1' is a reserved IP address (private, multicast, etc.).\"}";
        InputStream errorStream = new ByteArrayInputStream(responseString.getBytes());

        // mock classes
        URL mockedUrl = mock(URL.class);
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);

        when(mockedUrl.openConnection()).thenReturn(mockedCon);
        when(mockedCon.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
        when(mockedCon.getErrorStream()).thenReturn(errorStream);

        // call the method
        String result = LocationService.getLocationOfIp(mockedUrl);

        // assert and verify
        assertEquals("unknown location", result);
        verify(mockedUrl, times(1)).openConnection();
        verify(mockedCon, times(1)).setRequestMethod("GET");
        verify(mockedCon, times(1)).getResponseCode();
    }
}

