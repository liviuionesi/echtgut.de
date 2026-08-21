# Terraform — Azure

Provisions an ephemeral AKS cluster for cloud demos. See
[docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md) and
[docs/architecture/adr/001-zero-budget-azure-deploy.md](../../docs/architecture/adr/001-zero-budget-azure-deploy.md)
for why this exists and what it deliberately reuses from
[liviuionesi/lmdb.dev](https://github.com/liviuionesi/lmdb.dev).

## One-time bootstrap (before the first `terraform init`)

1. Create a Storage Account for remote state (any name; not managed by
   this config, to avoid a chicken-and-egg problem):
   ```bash
   az group create -n echtgut-tfstate -l eastus
   az storage account create -n <globally-unique-name> -g echtgut-tfstate -l eastus --sku Standard_LRS
   az storage container create -n tfstate --account-name <globally-unique-name>
   ```
2. `terraform init` with that account's details:
   ```bash
   cd azure
   terraform init \
     -backend-config="resource_group_name=echtgut-tfstate" \
     -backend-config="storage_account_name=<globally-unique-name>" \
     -backend-config="container_name=tfstate"
   ```
3. Supply the two required variables (no defaults, deliberately — see
   `variables.tf`):
   ```bash
   terraform apply \
     -var="alert_emails=[\"you@example.com\"]" \
     -var="budget_start_date=$(date -u +%Y-%m-01T00:00:00Z)"
   ```
4. Get cluster access:
   ```bash
   az aks get-credentials --resource-group $(terraform output -raw resource_group_name) --name $(terraform output -raw cluster_name)
   kubectl get nodes -o wide   # the node's ExternalIP is what deploy.yml points DuckDNS at
   ```
5. Roll out the app: `kubectl apply -k ../kubernetes/overlays/azure`
   (fill in `overlays/azure/secrets.env` from `secrets.env.example` first).

## Teardown

```bash
terraform destroy -var="alert_emails=[\"you@example.com\"]" -var="budget_start_date=..."
```

Or use the `Destroy` GitHub Actions workflow (passphrase-gated, requires
typing `DESTROY` to confirm). The `Cluster Idle Auto-Stop` workflow stops
(not destroys) the cluster automatically after 1 hour of backend
inactivity — data on the Postgres PVC survives a stop/start cycle either
way.

## CI (GitHub Actions) setup

`deploy.yml`/`destroy.yml`/`cluster-idle-stop.yml` expect these repo
**variables** (Settings → Secrets and variables → Actions → Variables):
`AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID`,
`TF_STATE_RESOURCE_GROUP`, `TF_STATE_STORAGE_ACCOUNT`, `TF_STATE_CONTAINER`,
`ALERT_EMAIL`, `DUCKDNS_DOMAIN` — and these **secrets**:
`DEPLOY_PASSPHRASE`, `DUCKDNS_TOKEN` (optional). `AZURE_CLIENT_ID` is an
Entra ID app registration federated for GitHub OIDC (no long-lived Azure
credential stored as a secret) — see the `azure:entra-app-registration`
skill if setting this up interactively.
