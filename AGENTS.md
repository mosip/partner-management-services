# AGENTS.md

## Repository Overview

This repository contains the source for MOSIP's **Partner Management (PMS)** module, which exposes REST APIs used to onboard and manage partners, policies, and policy groups within a MOSIP deployment. It has no bundled front-end; the reference UI lives in a separate repository, [partner-management-portal](https://github.com/mosip/partner-management-portal).

The module is made up of two independently deployable Spring Boot services:

1. **Partner management service** — `partner/partner-management-service`
2. **Policy management service** — `partner/policy-management-service`

Supporting Maven modules:

- `partner/pms-common` — shared code used by both services
- `partner/policy-validator` — policy schema/JSON validation logic

Other top-level directories:

- `api-test` — TestNG-based functional/API test rig for PMS (own build, own `CLAUDE.md` with detailed test-authoring guidance)
- `db_scripts`, `db_upgrade_scripts` — SQL schema and upgrade scripts (`mosip_pms` database)
- `deploy`, `helm` — Kubernetes deployment scripts and Helm charts
- `websub` — websub hub/consolidator components used by PMS for pub-sub notifications
- `performance-test` — performance/load test assets
- `docs` — supplementary documentation (e.g. `docs/configuration.md`)

## Technology Stack

- **Language / runtime**: Java, JDK 21.0.3 (`maven.compiler.source/target` = 21)
- **Build tool**: Maven, version 3.9.6 (multi-module reactor rooted at `partner/pom.xml`, parent artifact `io.mosip.pms:pms-parent`)
- **Framework**: Spring Boot 3.x
- **Persistence**: Hibernate/JPA against PostgreSQL (dialect must be set to `org.hibernate.dialect.PostgreSQLDialect`, not a version-suffixed dialect — see README)
- **Test frameworks**: JUnit (unit tests under each module's `src/test/java`), TestNG (functional API tests in `api-test`)
- **Coverage**: JaCoCo (`jacoco-maven-plugin`), reported to SonarCloud (`mosip_partner-management-services` project)
- **Containerization**: Docker (per-service `Dockerfile`), Helm charts for Kubernetes (`helm/pms-partner`, `helm/pms-policy`)

## Build & Test Commands

Build all PMS modules (skip tests, javadoc, and GPG signing for a fast local build):

```
cd partner
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

Run unit tests for a module (from within that module's directory, e.g. `partner/partner-management-service`):

```
mvn test
```

Build a Docker image for a given service:

```
cd <service-folder>   # e.g. partner/partner-management-service
docker build -f Dockerfile .
```

Build and run the functional API test rig (see `api-test/CLAUDE.md` for full details, test dependency graph, and Keycloak role table):

```
cd api-test
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
java -Dmodules=partner -Denv.user=<envUser> -Denv.endpoint=<baseUrl> -Denv.testLevel=smokeAndRegression \
  -jar target/apitest-pms-<version>-jar-with-dependencies.jar
```

## Configuration

PMS services do not ship application secrets/config in this repo. Runtime configuration is pulled from the centralized [mosip-config](https://github.com/mosip/mosip-config) repository:

- `partner-management-default.properties`
- `application-default.properties`

Local overrides live in each service's `src/main/resources/application-dev.properties` and `bootstrap.properties`. Feature flags that gate behavior by Key Manager version (`mosip.pms.ca.signed.partner.certificate.available`, `mosip.pms.oidc.client.available`, `mosip.pms.root.and.intermediate.certificates.available`) are documented in the README — do not assume all three are always `true` in a given deployment.

## Project Structure Notes

- Java packages under each service follow `io.mosip.pms.<domain>` (e.g. `partner`, `policy`, `oauth`, `oidc`, `device`, `user`, `notification`, `config`, `exception`, `tasklets`).
- Database schema/migration scripts are the source of truth for the `mosip_pms` schema — see `db_scripts/README.md` and `db_upgrade_scripts/README.MD` before changing entity mappings.
- `_rendered/` at the repo root and Helm `Chart.lock` files are generated artifacts; do not hand-edit them.

## Development Workflow

- Default integration branch is `develop`. Branch from `develop` for new work.
- This is a multi-module Maven reactor — after changing shared code in `pms-common` or `policy-validator`, rebuild the whole `partner` reactor (not just the dependent service) before testing.
- CI runs via GitHub Actions (`.github/workflows`): `push-trigger.yml` (Maven package build), `db-test.yml`, `chart-lint-publish.yml` (Helm chart lint/publish), `release-changes.yml`, `tag.yml`.
- SonarCloud quality gate runs against `develop`; keep coverage exclusions in `partner/pom.xml` (`sonar.coverage.exclusions`) in mind when adding new packages — DTOs, config, and constants are intentionally excluded from coverage targets, not from correctness expectations.

## Pull Request Guidelines

- Follow the existing commit/PR conventions visible in the repository's git history (conventional-style prefixes such as `fix:`, `feat:` are common).
- Keep changes scoped to one service/module where possible; PMS's two services are independently deployable and independently versioned.
- Do not commit generated files (`_rendered/`, `Chart.lock`, build `target/` directories) or environment-specific `application-dev.properties` credentials.
- License: this project is licensed under MPL 2.0 (`LICENSE`); do not introduce dependencies incompatible with that license without flagging it.

## Repository-Specific Considerations

- The `websub` directory bundles hub/consolidator components with their own README — treat it as a related-but-distinct component when scoping changes.
- `api-test/CLAUDE.md` already documents the functional test rig in depth (test YAML/Handlebars format, TestNG suite wiring, Keycloak roles, dependency graph). Prefer that file over re-deriving test-rig conventions here.
- API documentation is published externally at https://mosip.github.io/documentation/; this repo does not generate OpenAPI docs as part of the default build.
