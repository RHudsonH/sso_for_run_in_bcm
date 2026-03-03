# SSO for Run:ai in Base Command Manager

This project includes file, scripts and code needed to set up a laboratory SSO provider for Run:ai. 

As run is deployed by Base Command Manager (BCM) in this lab, the the BCM managed OpenLDAP server is used as the Directory Service.

## Architecture Overview

flowchart LR
    R([Run:ai]) -->|SSO Authentication| K([Keycloak])
    K -->|LDAP query - no mTLS| H([HAProxy])

    subgraph BCM Managed Infrastructure
        H([HAProxy]) -->|LDAP query - mTLS| L([BCM OpenLDAP])
    end

    style R fill:#0066cc,color:#fff,stroke:none
    style K fill:#4d9e3f,color:#fff,stroke:none
    style H fill:#e07b00,color:#fff,stroke:none
    style L fill:#8b0000,color:#fff,stroke:none
