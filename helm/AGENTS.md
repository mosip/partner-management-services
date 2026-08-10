# AGENTS.md — `helm/`

> Kubernetes Helm charts for the two independently deployable PMS services.
> Parent guide: [repo root `AGENTS.md`](../AGENTS.md).
> Installers that use these charts: [`deploy/AGENTS.md`](../deploy/AGENTS.md). Java app: [`partner/AGENTS.md`](../partner/AGENTS.md).

---

## 1. Charts

| Chart | Path | Deploys | Service image |
|-------|------|---------|----------------|
| `pms-partner` | `helm/pms-partner/` | Partner management service | `partner/partner-management-service` |
| `pms-policy` | `helm/pms-policy/` | Policy management service | `partner/policy-management-service` |

Both charts share the same shape (`Chart.yaml`, `values.yaml`, `templates/deployment.yaml`, `service.yaml`, `service-account.yaml`, `servicemonitor.yaml`, `virtualservice.yaml`, `extra-list.yaml`, `_helpers.tpl`) and depend on the Bitnami `common` chart for helper templates. Each is a standard single-container `Deployment` + `Service`, with a `VirtualService` for Istio routing and a `ServiceMonitor` for Prometheus scraping.

---

## 2. Install

```console
helm repo add mosip https://mosip.github.io
helm -n pms install pms-partner mosip/pms-partner --version <chart-version>
helm -n pms install pms-policy mosip/pms-policy --version <chart-version>
```

In practice, prefer [`deploy/pms/install.sh`](../deploy/AGENTS.md#3-deploypms-primary) over calling `helm install` directly — it also creates the namespace, sets Istio labels, and copies the ConfigMaps these charts expect.

Requires Kubernetes 1.12+ and Helm 3.1.0+ (per each chart's `README.md`).

---

## 3. Agent rules

### Do

1. Keep `pms-partner` and `pms-policy` chart structure in sync when adding a template (e.g. a new env var or probe) that applies to both services — they're meant to stay parallel.
2. Bump `Chart.yaml` `version` (not `appVersion`, which is left blank) when publishing a chart change; `deploy/pms/install.sh` pins to a specific `CHART_VERSION`, so keep that script's pinned version aligned when releasing.
3. Add new Helm values to `values.yaml` with sane defaults — installers may not set every value explicitly.
4. Lint charts locally (`helm lint helm/pms-partner`) before relying on CI's `chart-lint-publish.yml` workflow to catch issues.

### Do not

1. Hardcode environment-specific hosts/secrets into chart templates — pass them via `values.yaml` or `--set` from the installer scripts (see `deploy/pms/install.sh`'s `--set istio.corsPolicy...`).
2. Diverge the two charts' resource kinds (e.g. adding a `VirtualService` to one but not the other) without a documented reason — both services sit behind the same Istio ingress pattern.
3. Hand-edit `Chart.lock` — it is a generated lockfile for the `common` chart dependency.

---

*Last updated: 2026-08-10.*
