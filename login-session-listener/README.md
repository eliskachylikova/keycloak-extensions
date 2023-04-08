# Login Session Listener Extension

Keycloak extension that listens for login events and stores IP address information and more in a new user attribute `sessionInfo`. 

Developed to store historical user logins, since Keycloak only displays active sessions by default.

### Install the extension

To compile the extension in `/login-session-listener` directory run:
```
mvn package
```

JAR file will be generated in `/login-session-listener/target` directory.

Move the JAR file to `{KEYCLOAK_HOME}/providers` directory.

### Run Keycloak
For example by running this command in `{KEYCLOAK_HOME}/bin` directory:
```
./kc.sh start-dev
```

### Set up the extension in Keycloak Administration
- select `session-info-listener` event listener in `Realm Settings - Events`
- to view login history in Keycloak Account Console you need to use [custom theme](https://github.com/eliskachylikova/keycloak-extensions/tree/main/custom-theme)