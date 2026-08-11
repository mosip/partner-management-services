# AGENTS.md — `pms-common`

> Library (packaging `jar`, no `Dockerfile`, no `@SpringBootApplication`) — entities, DTOs, repositories, and shared services used by both PMS deployables.
> Parent guide: [`partner/AGENTS.md`](../AGENTS.md). Root: [repo root `AGENTS.md`](../../AGENTS.md).
> Depended on by: [`partner-management-service/AGENTS.md`](../partner-management-service/AGENTS.md), [`policy-management-service/AGENTS.md`](../policy-management-service/AGENTS.md).

---

## 1. Overview

Package layout under `io.mosip.pms.common`:

```text
config/       # shared Spring config
constant/     # shared constants
dto/          # shared request/response DTOs
entity/       # JPA entities mapped against the mosip_pms schema
exception/    # shared exception types
helper/       # helper utilities
repository/   # Spring Data JPA repositories
request/
response/
service/      # shared service logic
util/
validator/
```

Entities here map to tables defined in [`db_scripts/mosip_pms/ddl/`](../../db_scripts/mosip_pms/ddl/) (see [`db_scripts/AGENTS.md`](../../db_scripts/AGENTS.md) for the full guide) — keep entity fields and DDL columns in sync.

---

## 2. Build & test

```shell
cd pms-common
mvn test
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

This module produces a plain jar consumed by both deployable services — after any change here, rebuild the full `partner` reactor. From the repository root: `cd partner && mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true`; from inside `pms-common`, run `cd .. && mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true` instead. Then re-test both `partner-management-service` and `policy-management-service` — a service-only build can otherwise pick up a stale jar from the local `~/.m2` repository.

---

## 3. Agent rules

### Do

1. Put logic here only if it's genuinely shared by both `partner-management-service` and `policy-management-service` — otherwise it belongs in the consuming service module.
2. Keep entity field changes in sync with [`db_scripts/mosip_pms/ddl/`](../../db_scripts/AGENTS.md) and add a companion [`db_upgrade_scripts`](../../db_upgrade_scripts/AGENTS.md) migration for production-bound schema changes.
3. Rebuild the whole reactor and re-run both services' tests after changing shared repositories, entities, or DTOs — a downstream compile error or behavior change here affects both services silently otherwise.

### Do not

1. Add a `Dockerfile` or make this module independently deployable — it's intentionally a library.
2. Introduce a dependency from `pms-common` back onto `partner-management-service` or `policy-management-service` — dependencies flow one way, service → common.
3. Add a dependency from `pms-common` on `policy-validator` — the two library modules are independent of each other; neither depends on the other.

---

*Last updated: 2026-08-10.*
