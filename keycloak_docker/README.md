# Keyclock Docker

There are three services in the compose file.

1. postgres - The database backend for Keycloak
1. keycloak - The keycloak service itself which depends on postgres
1. ldap-proxy - An instance of HAProxy configured to proxy ldap traffic from keycloakd to BCM.

## Keycloak Providers

This lab, as designed, requires a custom keycloak provider. The code for this custom provider is located in this repository in the directory `keycloak_supplementarygroups_provider`. The .jar file built from that directory should be placed in `keycloak/image/keycloak_providers/` prior to building the keycloak image.

