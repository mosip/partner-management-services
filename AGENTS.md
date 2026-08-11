# AGENTS.md — MOSIP Partner Management Services (repo root)

> REST APIs to onboard and manage partners, policies, and policy groups within a MOSIP deployment. No bundled front-end — the reference UI lives in [partner-management-portal](https://github.com/mosip/partner-management-portal).

---

## Guide index

| Area | Path | Guide |
|------|------|-------|
| **Java / Maven** (partner-management-service, policy-management-service, pms-common, policy-validator) | `partner/` | [`partner/AGENTS.md`](partner/AGENTS.md) |
| Fresh DB install (DDL) | `db_scripts/` | [`db_scripts/AGENTS.md`](db_scripts/AGENTS.md) |
| Version upgrade SQL | `db_upgrade_scripts/` | [`db_upgrade_scripts/AGENTS.md`](db_upgrade_scripts/AGENTS.md) |
| Cluster install scripts | `deploy/` | [`deploy/AGENTS.md`](deploy/AGENTS.md) |
| K8s Helm charts | `helm/` | [`helm/AGENTS.md`](helm/AGENTS.md) |
| Performance/load tests (JMeter) | `performance-test/` | [`performance-test/AGENTS.md`](performance-test/AGENTS.md) |
| Functional API tests | `api-test/` | own [`api-test/CLAUDE.md`](api-test/CLAUDE.md) — not duplicated here |
| PMS data model (Excel/diagram, no code) | `design/` | none — reference assets only |

**Java build:** `cd partner && mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true` (JDK 21, Maven 3.9.6).

---

## Repository layout (repo root)

```text
partner-management-services/       # git repo root (this AGENTS.md)
├── partner/                       # Maven parent → see partner/AGENTS.md
│   ├── partner-management-service/
│   ├── policy-management-service/
│   ├── pms-common/
│   └── policy-validator/
├── db_scripts/                    # Greenfield DB create → db_scripts/AGENTS.md
├── db_upgrade_scripts/            # Incremental upgrades → db_upgrade_scripts/AGENTS.md
├── deploy/                        # Shell installers → deploy/AGENTS.md
├── helm/                          # Kubernetes Helm charts → helm/AGENTS.md
├── performance-test/              # JMeter load tests → performance-test/AGENTS.md
├── api-test/                      # TestNG functional test rig → own CLAUDE.md
├── design/                        # Data model diagrams/spreadsheet (no code)
└── docs/                          # Supplementary docs (e.g. docs/configuration.md)
```

`websub` and `_rendered` may appear in a local checkout but are **not tracked** on this branch — do not treat them as part of this repo's build.

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

```shell
cd partner
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

Run unit tests for a module (from within that module's directory, e.g. `partner/partner-management-service`):

```shell
mvn test
```

Full per-module build/run/Docker details: [`partner/AGENTS.md`](partner/AGENTS.md).

Build and run the functional API test rig (see `api-test/CLAUDE.md` for full details, test dependency graph, and Keycloak role table):

```shell
cd api-test
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
ENV_USER="your-env-user"
BASE_URL="https://your-base-url"
APITEST_VERSION="your-version"
java -Dmodules=partner -Denv.user="$ENV_USER" -Denv.endpoint="$BASE_URL" -Denv.testLevel=smokeAndRegression \
  -jar "target/apitest-pms-${APITEST_VERSION}-jar-with-dependencies.jar"
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
- Do not commit generated files (`_rendered/`, `Chart.lock`, build `target/` directories) or real credentials in any local override file (`application-dev.properties`, `bootstrap.properties`, or equivalents).
- License: this project is licensed under MPL 2.0 (`LICENSE`); do not introduce dependencies incompatible with that license without flagging it.

## Repository-Specific Considerations

- `api-test/CLAUDE.md` already documents the functional test rig in depth (test YAML/Handlebars format, TestNG suite wiring, Keycloak roles, dependency graph). Prefer that file over re-deriving test-rig conventions here.
- API documentation is published externally at https://mosip.github.io/documentation/; this repo does not generate OpenAPI docs as part of the default build.
- `design/` holds the PMS data-model spreadsheet and ER diagram (`design/data_model/`) — reference material only, not part of any build.

---

*Last updated: 2026-08-10.*
