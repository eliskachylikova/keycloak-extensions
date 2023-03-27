package cz.mendelu.pef.xchyliko.keycloak.extensions.emailNotifications;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

@JBossLog
public class LocationService {

    private static String COUNTRY_URL;
    private static String ACCOUNT_ID;
    private static String LICENSE_KEY;

    static {
        Properties prop = new Properties();
        try (InputStream input = LocationService.class.getClassLoader().getResourceAsStream("location.properties")) {
            prop.load(input);
            COUNTRY_URL = prop.getProperty("country_url");
            ACCOUNT_ID = prop.getProperty("account_id");
            LICENSE_KEY = prop.getProperty("license_key");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLocationOfIp(String ipAddress) throws IOException {

        // Set the Authorization header with account ID and license key
        String auth = ACCOUNT_ID + ":" + LICENSE_KEY;
        byte[] encodedAuth = Base64.encodeBytesToBytes(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        URL url = new URL(COUNTRY_URL + ipAddress);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", authHeader);

        // Get the response code
        int responseCode = con.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the response JSON using Jackson
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());

            // Return in format 'Country, Continent'
            return root.get("country").get("names").get("en").asText() + ", " + root.get("continent").get("names").get("en").asText();
        } else {
            // Handle the error response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            log.error("Error: " + response);
            return "unknown location";
        }
    }
}
