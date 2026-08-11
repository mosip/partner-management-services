# AGENTS.md — Maven Parent (`partner/`)

> Multi-module Maven reactor for MOSIP Partner Management Services (JDK 21, Spring Boot 3.x, parent artifact `io.mosip.pms:pms-parent`).
> For **database, Helm, deploy, and performance-test** work, see the [repo root `AGENTS.md`](../AGENTS.md) and folder guides: [`db_scripts`](../db_scripts/AGENTS.md), [`db_upgrade_scripts`](../db_upgrade_scripts/AGENTS.md), [`helm`](../helm/AGENTS.md), [`deploy`](../deploy/AGENTS.md), [`performance-test`](../performance-test/AGENTS.md).

---

## Module guides

| Module | Role | Agent guide |
|--------|------|-------------|
| `policy-validator` | Library — policy schema/JSON validation logic (`io.mosip.pms.policy.validator`) | [`policy-validator/AGENTS.md`](policy-validator/AGENTS.md) |
| `pms-common` | Library — shared entities, DTOs, repositories, exceptions used by both services (`io.mosip.pms.common`) | [`pms-common/AGENTS.md`](pms-common/AGENTS.md) |
| `policy-management-service` | HTTP deployable — policy/policy-group APIs | [`policy-management-service/AGENTS.md`](policy-management-service/AGENTS.md) |
| `partner-management-service` | HTTP deployable — partner, device, OAuth/OIDC, notification APIs | [`partner-management-service/AGENTS.md`](partner-management-service/AGENTS.md) |

---

## 1. Project overview

| Service | Module | Port | Servlet context path |
|---------|--------|------|----------------------|
| Partner management | `partner-management-service` | 9109 | `/v1/partnermanager` |
| Policy management | `policy-management-service` | 9107 | `/v1/policymanager` |

Both services share `spring.application.name=partner-management` and pull runtime config from the central [mosip-config](https://github.com/mosip/mosip-config) repo via Spring Cloud Config (`bootstrap.properties` per module). Neither library module (`pms-common`, `policy-validator`) is independently deployable — both are `packaging=jar` with no `Dockerfile` and no `@SpringBootApplication` class.

---

## 2. Module dependency graph

```text
partner-management-service (deployable, port 9109)
    └── depends on pms-common

policy-management-service (deployable, port 9107)
    ├── depends on pms-common
    └── depends on policy-validator

pms-common (library)
policy-validator (library)
```

`pms-common` and `policy-validator` do not depend on each other or on either deployable service.

---

## 3. Build & test

Build the whole reactor (required after touching `pms-common` or `policy-validator`, since both services depend on them):

```shell
cd partner
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

Run unit tests for a single module:

```shell
cd partner-management-service   # or policy-management-service, pms-common, policy-validator
mvn test
```

Build a Docker image for a deployable service:

```shell
cd partner-management-service   # or policy-management-service
docker build -f Dockerfile .
```

Run a service locally with a JVM system property (system properties precede `-jar`, not after):

```shell
java -Dspring.cloud.config.uri=http://localhost:8888 -Dspring.profiles.active=local \
  -jar target/partner-management-service-*.jar
```

---

## 4. Agent working rules

### Do

1. After changing shared code in `pms-common` or `policy-validator`, rebuild the **whole** `partner` reactor before testing either service — a service-only build can silently use a stale library jar from `~/.m2`.
2. Keep business/shared logic in `pms-common`/`policy-validator`; keep each service module to its own controllers, config, and domain-specific service code.
3. Preserve each service's servlet context path (`/v1/partnermanager`, `/v1/policymanager`) and port — other MOSIP components and `api-test` assume them.
4. Run `mvn test` in the affected module(s); run the `api-test` rig (see [`api-test/CLAUDE.md`](../api-test/CLAUDE.md)) for cross-service integration coverage.

### Do not

1. Add a direct dependency between `pms-common` and `policy-validator`, or between the two deployable services — they currently only depend on the shared libraries, not on each other.
2. Introduce a `Dockerfile` or `@SpringBootApplication` in `pms-common`/`policy-validator` — they are intentionally libraries, not deployables.
3. Skip rebuilding the reactor after a shared-module change and assume `mvn test` in one service module alone is sufficient.

---

*Last updated: 2026-08-10.*
