# Add Test Users

The ansible files in this directory are designed to add test users and groups to BCM

## Preparation

1. Set up Python dependencies
```shell
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

2. Install Ansible collections
```shell
ansible-galaxy collection install -r requirements.yaml
```

## Notes

The file group_vars/bcm/example-vault.yaml is an example. Adjust the default password or per-user passwords as necessary.


