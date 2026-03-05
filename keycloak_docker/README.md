# Keycloak Docker

This directory contains the Docker Compose stack that runs Keycloak and its supporting services. The stack provides OIDC-based SSO for Run:ai, federating users from BCM's OpenLDAP directory.

## Services

Three services are defined in `compose.yaml`:

| Service | Image | Description |
|---------|-------|-------------|
| `postgres` | postgres:16 | PostgreSQL database backend for Keycloak |
| `keycloak` | Custom build | Keycloak with the supplementary groups provider pre-installed. Exposed on port `443` (HTTPS). |
| `ldap-proxy` | haproxy:3.0 | Receives plain LDAP from Keycloak and forwards it to BCM OpenLDAP over LDAPS with mTLS. Internal to the Docker network — not exposed to the host. |

`keycloak` waits for `postgres` to pass its health check before starting, so Keycloak will not crash-loop on a cold start.

### Why HAProxy?

BCM's OpenLDAP requires clients to present a certificate (mTLS). Keycloak does not support client certificates on LDAP connections. HAProxy bridges this gap: it accepts plain LDAP from Keycloak on the internal `iam` Docker network and handles the mTLS handshake with BCM OpenLDAP on port 636.

## Prerequisites

Before starting the stack you need:

1. The custom provider `.jar` built and placed in `keycloak/image/keycloak_providers/` — see [`../keycloak_supplementarygroups_provider/README.md`](../keycloak_supplementarygroups_provider/README.md).
2. TLS certificates in place (see below).
3. A `.env` file configured (see below).

## Certificates

Both certificate directories are gitignored and must be populated manually.

### Keycloak TLS — `keycloak_certs/`

Keycloak serves HTTPS on port 443 using a certificate you provide. The certificate must be trusted by Run:ai (and by any browsers used to access the Keycloak admin console).

```
keycloak_certs/
├── Keycloak_BCM.crt    # TLS server certificate (or full chain)
└── Keycloak_BCM.key    # Private key
```

### HAProxy mTLS — `haproxy/certs/`

HAProxy needs the BCM LDAP CA certificate to verify the server, and a combined client cert+key PEM to authenticate itself.

```
haproxy/certs/
├── ldap-ca.pem         # CA that signed the BCM OpenLDAP server certificate
└── ldap-client.pem     # Client certificate and private key, concatenated
```

**Obtaining the CA certificate**

The BCM OpenLDAP CA certificate is on the head node at:

```
/cm/local/apps/openldap/etc/certs/ca.pem
```

Copy it to `haproxy/certs/ldap-ca.pem`.

**Generating the client certificate**

BCM provides the `cm-component-certificate` tool to generate component client certificates signed by the BCM CA. Run it from the **HA Active head node**:

```shell
cm-component-certificate --generate=<nodename>
```

Replace `<nodename>` with the hostname of the node that will run the HAProxy container (e.g. the head node itself). The generated certificate and key files are written to the current working directory unless `--outputdir` is specified:

```shell
cm-component-certificate --generate=<nodename> --outputdir=/tmp/haproxy-certs
```

This produces a `.crt` and a `.key` file. HAProxy requires them concatenated into a single PEM:

```shell
cat <nodename>.crt <nodename>.key > haproxy/certs/ldap-client.pem
```

## Environment Variables

Create a `.env` file in this directory before starting the stack. It is gitignored.

```env
# PostgreSQL
POSTGRES_DB=keycloak
POSTGRES_USER=keycloak
POSTGRES_PASSWORD=<random alphanumeric string>

# Keycloak bootstrap admin — change this password immediately after first login
KC_BOOTSTRAP_ADMIN_USERNAME=admin
KC_BOOTSTRAP_ADMIN_PASSWORD=<random alphanumeric string>

# IP address of the BCM OpenLDAP server
# Must match the CN or a SAN on the BCM OpenLDAP TLS certificate
BCM_LDAP_IP=10.141.255.254
```

Use long random alphanumeric strings for passwords to avoid shell interpolation issues.

> **Note:** `KC_BOOTSTRAP_ADMIN_PASSWORD` is only used by Keycloak to create the initial admin account. Once an admin user exists in the database, the variable is ignored on subsequent starts. Change the password in the Keycloak admin console after first login.

## Starting the Stack

```shell
# Build images and start all services in the background
docker compose up -d --build

# Follow logs
docker compose logs -f

# Stop containers (data volumes are preserved)
docker compose down
```

On first start, Postgres initialises its data directory before signalling healthy, so Keycloak may take a minute to appear.

Keycloak will be available at `https://<host>/`. The admin console is at `https://<host>/admin`.

## Custom Provider

The Keycloak image is built from `keycloak/image/Dockerfile`. It copies all `.jar` files from `keycloak/image/keycloak_providers/` into the Keycloak providers directory and runs `kc.sh build` to register them. The `keycloak_providers/` directory is gitignored.

The built image starts Keycloak in production mode (`start`), with HTTPS configured from the certificates mounted at `/opt/keycloak/conf/certs/`.
