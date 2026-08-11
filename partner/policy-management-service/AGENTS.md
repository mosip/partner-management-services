# AGENTS.md — `policy-management-service`

> HTTP deployable: policy and policy-group management APIs.
> Parent guide: [`partner/AGENTS.md`](../AGENTS.md). Root: [repo root `AGENTS.md`](../../AGENTS.md).
> Depends on: [`pms-common/AGENTS.md`](../pms-common/AGENTS.md), [`policy-validator/AGENTS.md`](../policy-validator/AGENTS.md).

---

## 1. Overview

- **Artifact**: `policy-management-service` (part of `io.mosip.pms:pms-parent` reactor, version `1.2.1-SNAPSHOT`)
- **Main class**: `io.mosip.pms.policy.PmpPolicyApplication`
- **Port**: `9107` — **context path**: `/v1/policymanager` (`bootstrap.properties`)
- **Application name**: `partner-management` (shared with `partner-management-service` for config server lookup)

Package layout under `io.mosip.pms.policy`:

```text
policy/
├── PmpPolicyApplication.java   # @SpringBootApplication entry point
├── config/                     # Spring config
├── controller/                 # PolicyManagementController — REST endpoints
├── dto/
├── errorMessages/
├── service/
└── util/
```

---

## 2. Build & run

```shell
cd policy-management-service
mvn test
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
docker build -f Dockerfile .
```

Run locally (JVM system properties precede `-jar`):

```shell
java -Dspring.cloud.config.uri=<config-server-url> -Dspring.profiles.active=local \
  -jar target/policy-management-service-*.jar
```

Local overrides: `src/main/resources/application-dev.properties`, `bootstrap.properties`. Do not commit real credentials into either.

---

## 3. Agent rules

### Do

1. Keep policy validation logic in [`policy-validator`](../policy-validator/AGENTS.md) — this module should call into it, not reimplement schema validation.
2. Keep shared entities/DTOs/repositories in [`pms-common`](../pms-common/AGENTS.md) rather than duplicating them here.
3. Preserve `/v1/policymanager` context path and port `9107` — other MOSIP components and `api-test` assume them.
4. Rebuild the full `partner` reactor (not just this module) after changing `pms-common` or `policy-validator`.

### Do not

1. Add a direct dependency on `partner-management-service` — the two services are independently deployable and versioned.
2. Duplicate DTOs/entities that already exist in `pms-common`.

---

*Last updated: 2026-08-10.*
