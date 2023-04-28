# Use an official maven runtime as a parent image
FROM maven:3.8.7-eclipse-temurin-11 as builder

# Set the working directory in the container
WORKDIR /app 

# Copy the pom.xml and source code to the container
COPY . .

# Build the JAR using Maven
RUN cd email-notifications && mvn package -Dmaven.test.skip

###################################

# Use an official maven runtime as a parent image
FROM maven:3.8.7-eclipse-temurin-11 as builder2

# Set the working directory in the container
WORKDIR /app 

# Copy the pom.xml and source code to the container
COPY . .

# Build the JAR using Maven
RUN cd login-session-listener && mvn package -Dmaven.test.skip

###################################

# Use Keycloak image
FROM quay.io/keycloak/keycloak:20.0.3

# Copy compiled Keycloak Extensions to image
COPY --from=builder /app/email-notifications/target/email-notifications-1.0.0.0-SNAPSHOT.jar /opt/keycloak/providers/
COPY --from=builder2 /app/login-session-listener/target/login-session-listener-1.0.0.0-SNAPSHOT.jar /opt/keycloak/providers/

# Copy custom Keycloak theme to image
# Directories must be created separately before copying the content
RUN mkdir -p /opt/keycloak/themes/login-history-theme/account 
COPY ./custom-theme/login-history-theme/account /opt/keycloak/themes/login-history-theme/account/
