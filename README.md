# keycloak-extensions

## Run in Docker

You can try the extensions by running Keycloak with these extensions in Docker using the given Dockerfile.

First build the image - in the root folder of this repo run:
```
docker build -t keycloak-extensions .
```

If you just want to build the plugin, run the following command from the root folder of this repo. You find the plugin jar e.g. in the following folder keycloak-extensions/email-notifications/target.
```
docker run -v $PWD:/app -w /app  maven:3.9.8-eclipse-temurin-17 mvn package -Dmaven.test.skip
```

Then run the container (admin user must be set up):
```
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin keycloak-extensions start-dev
```


## How to set up each extension

If you want to use these extensions in your Keycloak instance, use the following guides.

- [Email Notifications Keycloak Extension](https://github.com/eliskachylikova/keycloak-extensions/tree/main/email-notifications)
- [Login Session Listener Keycloak Extension](https://github.com/eliskachylikova/keycloak-extensions/tree/main/login-session-listener)
- [Custom theme for Keycloak Account Console](https://github.com/eliskachylikova/keycloak-extensions/tree/main/custom-theme)

**Requirements**

- Maven
- Keycloak (Quarkus) instance on your computer
  - avaliable on [Keycloak website](https://www.keycloak.org/downloads)

___
This product includes GeoLite2 data created by MaxMind, available from <a href="https://www.maxmind.com">https://www.maxmind.com</a>.
