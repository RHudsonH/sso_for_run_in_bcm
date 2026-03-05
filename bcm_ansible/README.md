# BCM Ansible — Add Users and Groups

This directory contains Ansible playbooks to batch-create users and groups in BCM using the `brightcomputing.bcm110` Ansible collection. Users created here are stored in BCM's managed OpenLDAP directory, making them available for Keycloak to federate for SSO.

## What the Playbook Does

`playbooks/add_users.yaml` runs three tasks against the BCM head node:

1. **Add Groups** — creates each group with a specified GID.
2. **Add Users** — creates each user with a UID, primary group, email, display name, and password.
3. **Add Users to Groups** — adds users to any supplementary groups defined in their group's `members` list.

## Configuration Files

Three files must be created from the provided examples before running the playbook.

### `inventory.yaml`

Defines the BCM head node to connect to:

```shell
cp inventory.yaml.example inventory.yaml
```

Edit `inventory.yaml` to set the correct hostname or IP address for your BCM head node.

### `group_vars/bcm/users.yaml`

Defines the groups and users to create. Each user needs a unique name, UID, and primary GID. Groups can optionally list `members` for supplementary group membership.

```shell
cp group_vars/bcm/users.yaml.example group_vars/bcm/users.yaml
```

Example structure:

```yaml
bcm_groups:
  - name: research1
    gid: 7001
  - name: run_users
    gid: 8001
    members:
      - research1

bcm_users:
  - name: research1
    uid: 7001
    fullName: "Research One"
    surname: One
    email: research1@example.com
    gid: 7001
    shell: /bin/false
```

### `group_vars/bcm/vault.yaml`

Stores user passwords, encrypted with Ansible Vault. The `vault_default_password` is used for any user not listed in `vault_user_passwords`.

```shell
cp group_vars/bcm/vault.yaml.example group_vars/bcm/vault.yaml
ansible-vault encrypt group_vars/bcm/vault.yaml
```

Edit `vault.yaml.example` before encrypting, or edit the encrypted file directly with `ansible-vault edit`.

## Setup

1. Create and activate a Python virtual environment:

```shell
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

2. Install the required Ansible collection:

```shell
ansible-galaxy collection install -r requirements.yaml
```

## Running the Playbook

```shell
ansible-playbook -i inventory.yaml playbooks/add_users.yaml --ask-vault-pass
```

Enter the Ansible Vault password when prompted.

To do a dry run without making changes:

```shell
ansible-playbook -i inventory.yaml playbooks/add_users.yaml --ask-vault-pass --check
```
