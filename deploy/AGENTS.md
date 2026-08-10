# AGENTS.md — `deploy/`

> Cluster-side shell installers that wrap Helm installs, ConfigMap/secret copies, and Keycloak client setup for PMS.
> Parent guide: [repo root `AGENTS.md`](../AGENTS.md).
> Charts: [`helm/AGENTS.md`](../helm/AGENTS.md). Java app: [`partner/AGENTS.md`](../partner/AGENTS.md).

---

## 1. Purpose

Run these scripts against a Kubernetes cluster (Rancher / ops host) to install, restart, or tear down PMS components, and to initialize the Keycloak clients PMS needs. All scripts assume `helm`, `kubectl`, and (for keycloak-init) `jq` are available and a working kube context (or a `kubeconfig` passed as `$1`).

---

## 2. Layout

```text
deploy/
├── copy_cm_func.sh           # shared helper: copy a configmap/secret across namespaces
├── pms/                      # primary PMS install (partner + policy manager charts)
│   ├── install.sh
│   ├── delete.sh
│   ├── restart.sh
│   └── README.md
├── pms-apitestrig/           # functional API test rig on cluster
│   ├── install.sh
│   ├── delete.sh
│   ├── values.yaml
│   └── README.md
└── keycloak/                 # Keycloak client/realm setup for PMS + eSignet
    ├── keycloak-init.sh
    └── keycloak-init-values.yaml
```

---

## 3. `deploy/pms` (primary)

| Script | Role |
|--------|------|
| `install.sh [kubeconfig]` | Create `pms` NS, Istio label, copy shared ConfigMaps, Helm install `pms-partner` + `pms-policy` |
| `delete.sh` | Tear down the install |
| `restart.sh` | Rolling restart of deployments |

`install.sh` flow: create namespace `pms` → enable Istio injection → copy `global`, `artifactory-share`, `config-server-share` ConfigMaps via `copy_cm_func.sh` → `helm install pms-partner mosip/pms-partner` and `helm install pms-policy mosip/pms-policy` (both pinned to `CHART_VERSION` in the script, CORS origin set from the `global` ConfigMap's `mosip-pmp-host`) → wait for rollout.

```bash
cd deploy/pms
./install.sh                 # uses current kube context
./install.sh /path/to/kubeconfig
./restart.sh
./delete.sh
```

Coordinate `CHART_VERSION` and chart names with [`helm/`](../helm/AGENTS.md) and the published `mosip` Helm repo.

---

## 4. `deploy/pms-apitestrig`

Installs the API test rig (`mosip/apitestrig` chart) against a deployed cluster as a scheduled CronJob. `install.sh` is interactive — it prompts for the cron hour, whether the cluster has a public domain + valid SSL (answering `n` mounts a self-signed cert into the container's Java keystore via an init-container), report retention days, a Slack webhook URL for failure notifications, and whether eSignet is deployed (skips eSignet test cases if not).

```bash
cd deploy/pms-apitestrig
./install.sh
./delete.sh
```

Review `values.yaml` first to confirm which modules are enabled. Run manually via Rancher UI or:

```bash
kubectl --kubeconfig=/path/to/k8s-config-file -n apitestrig create job --from=cronjob/cronjob-name job-name
```

Functional test sources live in repo `api-test/` (see [`api-test/CLAUDE.md`](../api-test/CLAUDE.md)).

---

## 5. `deploy/keycloak`

`keycloak-init.sh` copies Keycloak env-var ConfigMaps and secrets into the `pms` namespace, then runs the `mosip/keycloak-init` chart (`keycloak-init-values.yaml`) to create/update the `mosip-pms-client` Keycloak client and its realm attributes, syncing the resulting client secret back into the `keycloak` namespace's `keycloak-client-secrets` secret.

```bash
cd deploy/keycloak
./keycloak-init.sh
```

Run this before `deploy/pms/install.sh` — PMS services expect the Keycloak client to already exist.

---

## 6. Agent rules

### Do

1. Use `deploy/pms/install.sh` as the primary PMS cluster entry point.
2. Keep `CHART_VERSION` in `install.sh` scripts aligned with the published `mosip` Helm repo and [`helm/`](../helm/AGENTS.md) chart versions.
3. Copy required ConfigMaps/secrets (`global`, `artifactory-share`, `config-server-share`, Keycloak) into the target namespace via `copy_cm_func.sh` before installing charts that depend on them.
4. Run `deploy/keycloak/keycloak-init.sh` before relying on PMS's Keycloak client existing.

### Do not

1. Hardcode secrets into install scripts — they already pull credentials from cluster Secrets (`keycloak-client-secrets`, `s3`, `postgres-postgresql`).
2. Change the namespace (`pms`) or chart names in one script without updating the others that assume them.
3. Skip the `pms-apitestrig` public-domain/SSL prompt by guessing an answer for a real cluster — it changes whether an init-container is added to the deployment.

---

*Last updated: 2026-08-10.*
