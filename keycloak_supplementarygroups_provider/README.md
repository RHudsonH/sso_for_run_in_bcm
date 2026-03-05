# Keycloak Supplementary Groups Provider

A custom Keycloak OIDC protocol mapper that reads a user's group memberships from Keycloak (federated from BCM's OpenLDAP) and injects two claims into issued tokens:

| Claim | Type | Content |
|-------|------|---------|
| `GROUPS` | `string[]` | LDAP group names the user belongs to |
| `SUPPLEMENTARYGROUPS` | `integer[]` | `gidNumber` values for those groups |

Run:ai requires these claims for workload identity and permissions mapping. The standard Keycloak group mappers only expose group names or paths; this mapper additionally extracts and exposes the numeric `gidNumber` LDAP attribute that BCM uses to identify groups.

## Prerequisites

Before the mapper can populate `SUPPLEMENTARYGROUPS`, Keycloak must be syncing group membership **and** the `gidNumber` attribute from LDAP. This requires an LDAP group mapper in Keycloak's user federation config with `gidNumber` listed as a group attribute to synchronise. See the main [README.md](../README.md) for full Keycloak configuration steps.

## Building

A `Dockerfile` is provided in this directory to build the `.jar` without requiring a local Java or Maven installation.

```shell
# From the keycloak_supplementarygroups_provider/ directory:

# Build the builder image
docker build -t keycloak-gid-mapper-builder .

# Run the Maven build inside the container, writing output to the local source tree
docker run --rm \
    -v "$PWD/keycloak-gid-mapper:/build/keycloak-gid-mapper" \
    keycloak-gid-mapper-builder \
    bash -c "cd /build/keycloak-gid-mapper && mvn package"
```

The `.jar` will be written to:

```
keycloak-gid-mapper/target/keycloak-gid-mapper-1.0.0.jar
```

## Installation

The `.jar` must be placed in the Keycloak image build context **before** the Docker Compose stack is built. The `keycloak/image/Dockerfile` copies everything in `keycloak_providers/` into the Keycloak image at build time and runs `kc.sh build` to register the providers.

```shell
cp keycloak-gid-mapper/target/keycloak-gid-mapper-1.0.0.jar \
   ../keycloak_docker/keycloak/image/keycloak_providers/
```

Then build (or rebuild) the stack:

```shell
cd ../keycloak_docker
docker compose up -d --build
```

## Keycloak Configuration

After the stack is running, add the mapper to the OIDC client that Run:ai will use:

1. Log in to the Keycloak admin console.
2. Go to **Clients** → select your Run:ai client → **Client scopes** tab.
3. Open the client's dedicated scope (named `<client-id>-dedicated`).
4. Click **Add mapper** → **By configuration**.
5. Select **Supplementary Groups (GID) Mapper**.
6. Set **Token Claim Name** to `SUPPLEMENTARYGROUPS`.
7. Enable **Add to ID token** and **Add to access token**.
8. Save.

The `GROUPS` claim (group names) is added automatically alongside `SUPPLEMENTARYGROUPS` by the same mapper — no separate mapper is needed for it.
