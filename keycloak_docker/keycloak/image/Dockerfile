FROM quay.io/keycloak/keycloak:26.5.3

COPY keycloak_providers/*.jar /opt/keycloak/providers/

RUN /opt/keycloak/bin/kc.sh build --db=postgres

