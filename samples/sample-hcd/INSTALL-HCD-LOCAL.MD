# Setup to Pull HCD from ECR

This guide provides step-by-step instructions for setting up AWS CLI and Podman/Docker to pull HCD (Hyper-Converged Database) images from the DataStax ECR (Elastic Container Registry) repository.

## Table of Contents

1. [Local Laptop Setup](#local-laptop-setup)
2. [GitHub Actions Setup (CI/CD)](#github-actions-setup-cicd)
3. [Re-authentication](#re-authentication)
4. [Troubleshooting](#troubleshooting)

---

## Local Laptop Setup

### Prerequisites

- AWS CLI installed
- Podman (or Docker) installed
- Active Okta account with appropriate permissions

### Step 1: Install Required Tools

```bash
# Install AWS CLI (if not already installed)
# Visit: https://aws.amazon.com/cli/

# Install Podman (if not already installed)
# Visit: https://podman.io/getting-started/installation
```

### Step 2: Configure AWS CLI with SSO

Ensure you are logged into Okta, then configure AWS CLI:

```bash
aws configure sso
```

Provide the following information when prompted:

| Prompt | Value                                    |
|--------|------------------------------------------|
| **Session name** | Choose any name (e.g., `alala`)          |
| **Start URL** | `https://<redacted>.awsapps.com/start/#` |
| **SSO Region** | `us-west-2`                              |
| **SSO registration scopes** | Leave as default `[sso:account:access]`  |

### Step 3: Authorize via Browser

1. A browser window will open automatically
2. Complete the Okta authentication
3. Click **"Allow access to your data"** when prompted
4. You'll see a green confirmation message
5. Close the browser tab and return to the terminal

### Step 4: Select AWS Account

When prompted, select the **`engops-shared`** account.

**Expected output:**

```
There are 3 AWS accounts available to you.
Using the account ID <redacted>
The only role available to you is: DATAAPI-ENG-DEV_ENG-SHAR
Using the role name "DATAAPI-ENG-DEV_ENG-SHAR"
Default client Region [us-west-2]:
CLI default output format (json if not specified) [None]:
Profile name [DATAAPI-ENG-DEV_ENG-SHAR-<redacted>]:

To use this profile, specify the profile name using --profile, as shown:

aws sts get-caller-identity --profile DATAAPI-ENG-DEV_ENG-SHAR-<redacted>
```

### Step 5: Verify Configuration

Test your configuration:

```bash
aws sts get-caller-identity --profile DATAAPI-ENG-DEV_ENG-SHAR-<redacted>
```

Check your AWS configuration file (`~/.aws/config`):

```ini
[profile DATAAPI-ENG-DEV_ENG-SHAR-<redacted>]
sso_session = Engops
sso_account_id = <redacted>
sso_role_name = DATAAPI-ENG-DEV_ENG-SHAR
region = us-west-2

[sso-session Engops]
sso_start_url = https://<redacted>awsapps.com/start/#
sso_region = us-west-2
sso_registration_scopes = sso:account:access
```

> **Note:** No credentials file is created. Authentication is cached in `~/.aws/sso/` directory.

### Step 6: Login to ECR with Podman

Authenticate Podman with ECR:

```bash
aws ecr get-login-password \
  --profile DATAAPI-ENG-DEV_ENG-SHAR-<redacted> \
  --region us-west-2 \
  | podman login --username AWS --password-stdin \
    <redacted>.dkr.ecr.us-west-2.amazonaws.com
```

### Step 7: Pull HCD Image

Pull the HCD image from ECR:

```bash
podman pull <redacted>/engops-shared/hcd/prod/hcd:1.2.1-early-preview
```

**Success!** You can now use the HCD image locally.

---

## Re-authentication

AWS SSO tokens expire periodically. If you encounter an authentication error:

```
Error when retrieving token from sso: Token has expired and refresh failed
```

Re-authenticate with:

```bash
aws sso login --profile DATAAPI-ENG-DEV_ENG-SHAR-<redacted>
```

This will open a browser tab for Okta authentication. Once completed, you can retry your previous command.

---

## Start HCD image





## Additional Resources

- [AWS CLI SSO Configuration](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-sso.html)
- [Amazon ECR User Guide](https://docs.aws.amazon.com/ecr/)
- [Podman Documentation](https://docs.podman.io/)
- [GitHub Actions OIDC](https://docs.github.com/en/actions/deployment/security-hardening-your-deployments/about-security-hardening-with-openid-connect)

---

**Last Updated:** September 2025