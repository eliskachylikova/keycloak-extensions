import cz.mendelu.pef.xchyliko.keycloak.extensions.emailNotifications.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Test
    void getLocationOfIp_success() throws IOException {

        // example of what can geolocation API return
        String responseString = "{\"country\":{\"names\":{\"en\":\"United States\"}},\"continent\":{\"names\":{\"en\":\"North America\"}}}";
        InputStream inputStream = new ByteArrayInputStream(responseString.getBytes());

        // mock classes
        URL mockedUrl = mock(URL.class);
        HttpURLConnection mockedCon = mock(HttpURLConnection.class);

        when(mockedUrl.openConnection()).thenReturn(mockedCon);
        when(mockedCon.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockedCon.getInputStream()).thenReturn(inputStream);

        // call the method
        String result = LocationService.getLocationOfIp(mockedUrl);

        // assert and verify
        assertEquals("United States, North America", result);
        verify(mockedUrl, times(1)).openConnection();
        verify(mockedCon, times(1)).setRequestMethod("GET");
        verify(mockedCon, times(1)).getResponseCode();
    }

//    @Test
//    void getLocationOfIp_success() throws IOException {
//
//        try (MockedStatic<LocationService> service = Mockito.mockStatic(LocationService.class)) {
//            service.when(() -> LocationService.getLocationOfIp("1.2.3.4")).thenReturn("France, Europe");
//            assertEquals(LocationService.getLocationOfIp("1.2.3.4"), "France, Europe");
//        }
//
//        assertEquals(LocationService.getLocationOfIp("1.2.3.4"), "France, Europe");
//    }
//
//    @Test
//    void getLocationOfIp_fail() throws IOException {
//
//        try (MockedStatic<LocationService> service = Mockito.mockStatic(LocationService.class)) {
//            service.when(() -> LocationService.getLocationOfIp("192.0.0.1")).thenReturn(null);
//            assertEquals(LocationService.getLocationOfIp("192.0.0.1"), null);
//        }
//
//        assertEquals(LocationService.getLocationOfIp("192.0.0.1"), "unknown location");
//    }
}

