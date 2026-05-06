# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Identity

- **Module**: `partner-management-services` API test rig
- **Artifact**: `apitest-pms` (`io.mosip.partner:apitest-pms:1.2.1-SNAPSHOT`)
- **Main class**: `io.mosip.testrig.apirig.partner.testrunner.MosipTestRunner`
- **FAT JAR name**: `apitest-pms-1.2.1-SNAPSHOT-jar-with-dependencies.jar`

## Commands

### Build
```powershell
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```

### Run via JAR
```powershell
java -Dmodules=partner -Denv.user=api-internal.<envName> -Denv.endpoint=<baseUrl> -Denv.testLevel=smokeAndRegression -jar target/apitest-pms-1.2.1-SNAPSHOT-jar-with-dependencies.jar
```
- `env.user` format: `api-internal.qa21` (matches environment prefix in properties)
- `env.testLevel`: `smoke` (positive only) | `regression` | `smokeAndRegression`

### Run a subset of tests
Set `testCasesToExecute` in `pms.properties` to a comma-separated list of `uniqueIdentifier` values (e.g., `TC_PMS_definePolicyGroup_01`). Leave empty to run all.

### IDE execution
Main class: `io.mosip.testrig.apirig.partner.testrunner.MosipTestRunner`
VM args: `-Dmodules=partner -Denv.user=... -Denv.endpoint=... -Denv.testLevel=...`
Also uncomment `generateDependencyJson` in `pms.properties` for first local run to rebuild the dependency graph.

## Architecture of This Module

### Test Execution Flow
```
MosipTestRunner.main()
  → PMSConfigManager.init()          loads pms.properties
  → Keycloak user creation           creates roles/users for test run
  → DB cleanup (PMSUtil.DbCleanRevamp)
  → TestNG runs pmsMasterTestSuite.xml → pmsSuite.xml
      → each <test> maps to a testscripts/* class + YAML file
  → System.exit(0)
```

### Test Definition: YAML + Handlebars
Each test scenario lives under `src/main/resources/pms/<FeatureName>/`:
- `<FeatureName>.yml` — test cases, one key per scenario:
  - `endPoint`, `restMethod`, `role`, `uniqueIdentifier`
  - `input` (JSON body with `$TIMESTAMP$`, `$RUNCONTEXT$`, `$REMOVE$` placeholders)
  - `output` (expected response JSON for assertion)
  - `inputTemplate` / `outputTemplate` — paths to `.hbs` files
- `*.hbs` files — Handlebars templates for request/response body rendering

### TestNG Suite Wiring (`testNgXmlFiles/pmsSuite.xml`)
Each `<test>` block declares:
- `ymlFile` — resource path to the YAML file
- `idKeyName` — comma-separated response fields to capture as runtime IDs for downstream tests
- `pathParams` — path-segment values injected into endpoints
- `reqKeyName` — request field to extract for chained tests

Tests have a strict dependency ordering — reordering entries in `pmsSuite.xml` breaks dependent tests.

### Test Script Classes (`testscripts/`)
21 classes covering every HTTP verb + variation. Choose the right one when adding a new test to `pmsSuite.xml`:

| Class | When to use |
|---|---|
| `SimplePost` | POST, no captured IDs |
| `SimplePostForAutoGenId` | POST, response contains auto-generated ID to reuse |
| `PostWithBodyAndPathParams` | POST with path segments |
| `PostWithBodyAndPathParamsAndAutoGenId` | POST with path segments + ID capture |
| `SimplePostWithoutBody` | POST with no request body |
| `PostWithOnlyPathParam` | POST where path param is the entire payload |
| `GetWithParam` | GET with query/path params |
| `GetWithParamForAutoGenId` | GET + ID capture |
| `GetWithQueryParam` | GET with only query string |
| `SimplePut` | PUT, no path params |
| `PutWithPathParamsAndBody` | PUT with path segments |
| `PutWithPathParamsAndBodyForAutoGenId` | PUT with path segments + ID capture |
| `SimplePatch` | PATCH, no path params |
| `SimplePatchForAutoGenId` | PATCH + ID capture |
| `PatchWithPathParam` | PATCH with a single path segment |
| `PatchWithPathParamsAndBody` | PATCH with multiple path segments |
| `PatchWithPathParamsAndBodyForAutoGenId` | PATCH + path segments + ID capture |
| `DownloadRootCertificate` | Certificate download (binary response) |
| `AuditValidator` | Asserts audit log entries after another test |
| `DBValidator` | Asserts DB row state after another test |
| `DBIntegration` | Cross-DB integration checks |

### PMS-Specific Utilities (`utils/`)
- **`PMSConfigManager`** — extends `ConfigManager`; calls `init()` to load `pms.properties`; exposes KeyManager DB connection details (`getKeymangrDbUrl/User/Pass()`)
- **`PMSConstants`** — two error-message constants for feature-not-supported cases (`FEATURE_NOT_SUPPORTED_PMSREVAMP`, `FEATURE_NOT_SUPPORTED_PMSREVAMP_FOR_OIDC`)
- **`PMSUtil`** — extends `AdminTestUtil`; key methods:
  - `inputStringKeyWordHandeler()` — replaces `$IDPREDIRECTURI$`, `$LICENSE_KEY_NAME$` with runtime values
  - `DbCleanRevamp()` — runs cleanup SQL from the three `.txt` files in `config/`
  - `validateResponse()` — skips test if response indicates feature not supported
  - `normalizeDateFields()` — strips time from date fields before assertion
- **`ExtendedDBManager`** — batch JDBC execution for cleanup queries

### Configuration (`src/main/resources/config/pms.properties`)
Key tunable fields (environment-specific):
- `env.endpoint` / `env.user` — set via `-D` flags at runtime; do not hardcode
- `db.*` — DB host, port, credentials for PMS, KeyManager, Audit schemas
- `keycloak.*` — IAM URL, admin password, client secrets
- `testCasesToExecute` — filter to specific test IDs (empty = all)
- `enableDebug` — set to `yes` for verbose REST Assured logging
- `generateDependencyJson` — uncomment only for local IDE runs to rebuild `testCaseInterDependency.json`

DB cleanup query files (used by `DbCleanRevamp()`):
- `config/partnerRevampDataDeleteQueries.txt` — PMS schema
- `config/partnerRevampDataDeleteQueriesForKeyMgr.txt` — KeyManager schema
- `config/partnerRevampDataDeleteQueriesForIDA.txt` — IDA schema

### Test Dependency Graph
`src/main/resources/config/testCaseInterDependency.json` (97 KB) maps which test cases must run before others. This file is auto-generated — do not edit manually. Regenerate by enabling `generateDependencyJson` in `pms.properties` and running in IDE mode.

## Adding a New Test Case

1. Create or extend a YAML file under `src/main/resources/pms/<FeatureName>/`
2. Assign a unique `uniqueIdentifier` (format: `TC_PMS_<featureName>_<NN>`)
3. Add `.hbs` templates if the request/response body needs dynamic rendering
4. Add a `<test>` entry to `pmsSuite.xml` pointing to the correct testscript class and `ymlFile`
5. If the new test produces an ID consumed by later tests, set `idKeyName` in the XML and add `$IDPREDIRECTURI$` or equivalent keyword in `PMSUtil.inputStringKeyWordHandeler()`

## Keycloak Roles Used

| Role | Test users |
|---|---|
| `AUTH_PARTNER` | 111999, 111666 |
| `PARTNER_ADMIN` + `POLICYMANAGER` | 111777 |
| `DEVICE_PROVIDER` + `POLICYMANAGER` | 111998 |
| `DEVICE_PROVIDER` | 111994 |
| `FTM_PROVIDER` | 111888 |