# Partner Management

Helm chart for installing Partner Management Partner module.  The module is generally external facing for other partners to connect a receive events.

## TL;DR

```console
$ helm repo add mosip https://mosip.github.io
$ helm install my-release mosip/pms-partner
```
## Prerequisites

- Kubernetes 1.12+
- Helm 3.1.0
- PV provisioner support in the underlying infrastructure
- ReadWriteMany volumes for deployment scaling

