# Keycloak Supplementary Groups Provider

A custom Keycloak OIDC protocol mapper that reads a user's LDAP group memberships and adds two claims to tokens issued by Keycloak:

| Claim | Content |
|-------|---------|
| `GROUPS` | List of LDAP group names the user belongs to |
| `SUPPLEMENTARYGROUPS` | List of integer `gidNumber` values for those groups |

Run:ai uses these claims for workload identity and permissions mapping.

## Building

A `Dockerfile` is provided to build the `.jar` in an isolated environment without requiring a local Java/Maven installation.

```shell
# Build the builder image
docker build -t keycloak-gid-mapper-builder .

# Run the Maven build
docker run --rm \
    -v "$PWD/keycloak-gid-mapper:/build/keycloak-gid-mapper" \
    keycloak-gid-mapper-builder \
    bash -c "cd /build/keycloak-gid-mapper && mvn package"
```

The `.jar` will be output to `keycloak-gid-mapper/target/keycloak-gid-mapper-1.0.0.jar`.

## Installation

Copy the `.jar` into the Keycloak image providers directory before building the Docker stack:

```shell
cp keycloak-gid-mapper/target/keycloak-gid-mapper-1.0.0.jar \
   ../keycloak_docker/keycloak/image/keycloak_providers/
```

The `keycloak/image/Dockerfile` copies all `.jar` files from `keycloak_providers/` into the Keycloak image at build time.

## Keycloak Configuration

After deploying the stack, add the mapper to your OIDC client in Keycloak:

1. Open the Keycloak admin console and navigate to your realm.
2. Go to **Clients** → your Run:ai client → **Client scopes**.
3. Open the dedicated scope, then **Add mapper** → **By configuration**.
4. Select **Supplementary Groups (GID) Mapper**.
5. Set the **Token Claim Name** (e.g. `SUPPLEMENTARYGROUPS`) and enable inclusion in the ID token and access token as required.
