import cz.mendelu.pef.xchyliko.keycloak.extensions.emailNotifications.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Test
    void getLocationOfIp_success() throws IOException {

        try (MockedStatic<LocationService> service = Mockito.mockStatic(LocationService.class)) {
            service.when(() -> LocationService.getLocationOfIp("1.2.3.4")).thenReturn("France, Europe");
            assertEquals(LocationService.getLocationOfIp("1.2.3.4"), "France, Europe");
        }

        assertEquals(LocationService.getLocationOfIp("1.2.3.4"), "France, Europe");
    }

    @Test
    void getLocationOfIp_fail() throws IOException {

        try (MockedStatic<LocationService> service = Mockito.mockStatic(LocationService.class)) {
            service.when(() -> LocationService.getLocationOfIp("192.0.0.1")).thenReturn(null);
            assertEquals(LocationService.getLocationOfIp("192.0.0.1"), null);
        }

        assertEquals(LocationService.getLocationOfIp("192.0.0.1"), "unknown location");
    }
}

