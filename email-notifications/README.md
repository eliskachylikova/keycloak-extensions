# Email Notifications Extension

Keycloak extension that sends a notification email if you log in from a new IP address.

### Set up Location Service

Before using the extension you need to set up `location.properties` file. See [`/email-notifications/src/main/resources/location.properties.example`.](https://github.com/eliskachylikova/keycloak-extensions/blob/50fca6d474f1f7287aa059cda7c5f8d191d0ef16/email-notifications/src/main/resources/location.properties.example) 

This extensions uses [MaxMind GeoLite2 Free Geolocation Data](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data?lang=en). To get your own License Key you need to create account on their website.

### Install the extension

To compile the extension in `/email-notifications` directory run:
```
mvn package
```

JAR file will be generated in `/email-notifications/target` directory.

Move the JAR file to `{KEYCLOAK_HOME}/providers` directory.

### Run Keycloak
For example by running this command in `{KEYCLOAK_HOME}/bin` directory:
```
./kc.sh start-dev
```

### Set up the extension in Keycloak Administration
- set up e-mail server in `Realm Settings - Email`
- select `log-in-new-ip-email-notifications` event listener in `Realm Settings - Events`
- obviously, users must have an email address set up for this extension to work


___
This product includes GeoLite2 data created by MaxMind, available from <a href="https://www.maxmind.com">https://www.maxmind.com</a>.