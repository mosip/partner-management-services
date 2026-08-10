# AGENTS.md — `policy-validator`

> Library (packaging `jar`, no `Dockerfile`, no `@SpringBootApplication`) — policy schema/JSON validation logic.
> Parent guide: [`partner/AGENTS.md`](../AGENTS.md). Root: [repo root `AGENTS.md`](../../AGENTS.md).
> Depended on by: [`policy-management-service/AGENTS.md`](../policy-management-service/AGENTS.md).

---

## 1. Overview

Package layout under `io.mosip.pms.policy.validator`:

```text
validator/
├── spi/
│   └── PolicyValidator.java          # validator interface (SPI)
├── impl/
│   └── PolicySchemaValidator.java    # JSON-schema based implementation
├── constants/
└── exception/
```

`PolicySchemaValidator` implements the `PolicyValidator` SPI to validate policy JSON payloads against a schema before `policy-management-service` persists a policy.

---

## 2. Build & test

```shell
cd policy-validator
mvn test
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

Only `policy-management-service` consumes this module — `partner-management-service` does not depend on it. After a change here, rebuild the full `partner` reactor and re-test `policy-management-service`.

---

## 3. Agent rules

### Do

1. Implement new validation rules against the `PolicyValidator` SPI (`spi/`), not by bypassing it in the consuming service.
2. Keep validation error messages/constants in `constants/` and `exception/` rather than inlining them in `impl/`.
3. Rebuild the reactor and re-test `policy-management-service` after any change here.

### Do not

1. Add a dependency on `pms-common` entities/repositories unless genuinely needed — this module is meant to be a narrow, schema-focused validator.
2. Make this module independently deployable (`Dockerfile`, `@SpringBootApplication`) — it's a library consumed only by `policy-management-service`.

---

*Last updated: 2026-08-10.*
