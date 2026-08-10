# AGENTS.md — `partner-management-service`

> HTTP deployable: partner, device (SBI/FTM), OAuth/OIDC client, notification, and MISP license APIs.
> Parent guide: [`partner/AGENTS.md`](../AGENTS.md). Root: [repo root `AGENTS.md`](../../AGENTS.md).
> Depends on: [`pms-common/AGENTS.md`](../pms-common/AGENTS.md).

---

## 1. Overview

- **Artifact**: `partner-management-service` (part of `io.mosip.pms:pms-parent` reactor, version `1.2.1-SNAPSHOT`)
- **Main class**: `io.mosip.pms.service.PartnerManagementService`
- **Port**: `9109` — **context path**: `/v1/partnermanager` (`bootstrap.properties`)
- **Application name**: `partner-management` (shared with `policy-management-service` for config server lookup)

Package layout under `io.mosip.pms`:

```text
config/          # Spring config
service/         # PartnerManagementService — @SpringBootApplication entry point
partner/         # partner CRUD, constant, controller, dto, keycloak, manager, misp, request, response, service, util
device/          # SBI/device + FTM chip management: authdevice, controller, dto, request, response, validator
oauth/client/    # OAuth client management
oidc/client/     # OIDC client management (see mosip.pms.oidc.* properties)
notification/job/  # scheduled notification jobs (cert/SBI/API-key expiry, weekly summaries)
tasklets/service, util  # Spring Batch tasklets
user/controller, service  # user-facing endpoints
exception/       # shared exception types
```

The `mosip.pms.batch.job.*` cron properties in `bootstrap.properties` drive the jobs under `notification/job` — expiry notifications for root/intermediate/partner certs, FTM chips, SBIs, API keys, MISP license keys, plus weekly summaries and past-notification cleanup.

---

## 2. Build & run

```shell
cd partner-management-service
mvn test
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
docker build -f Dockerfile .
```

Run locally (JVM system properties precede `-jar`):

```shell
java -Dspring.cloud.config.uri=<config-server-url> -Dspring.profiles.active=local \
  -jar target/partner-management-service-*.jar
```

Local overrides: `src/main/resources/application-dev.properties`, `bootstrap.properties`. Do not commit real credentials into either.

---

## 3. Agent rules

### Do

1. Keep shared entities/DTOs/repositories in [`pms-common`](../pms-common/AGENTS.md) rather than duplicating them here.
2. Gate behavior that depends on Key Manager capabilities behind the existing feature flags (`mosip.pms.ca.signed.partner.certificate.available`, `mosip.pms.oidc.client.available`, `mosip.pms.root.and.intermediate.certificates.available`) instead of assuming they're always `true`.
3. Preserve `/v1/partnermanager` context path and port `9109` — other MOSIP components and `api-test` assume them.
4. When adding a scheduled job, follow the existing `notification/job` pattern and add a corresponding `mosip.pms.batch.job.*.cron.schedule` property.
5. Rebuild the full `partner` reactor (not just this module) after changing `pms-common`.

### Do not

1. Add a direct dependency on `policy-management-service` — the two services are independently deployable and versioned.
2. Duplicate DTOs/entities that already exist in `pms-common`.
3. Assume all Key Manager feature flags are enabled in every deployment.

---

*Last updated: 2026-08-10.*
