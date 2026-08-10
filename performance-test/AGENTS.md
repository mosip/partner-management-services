# AGENTS.md — `performance-test/`

> Apache JMeter load-test scripts for PMS partner/policy/OIDC/API-key/device/FTM workflows.
> Parent guide: [repo root `AGENTS.md`](../AGENTS.md).
> Related: [`api-test/CLAUDE.md`](../api-test/CLAUDE.md) (functional, not load, testing).

---

## 1. Purpose

Measure throughput/latency of PMS's partner self-registration, policy request, OIDC client, API key, device/SBI, and FTM chip flows against a deployed environment, using Apache JMeter.

---

## 2. Layout

```text
performance-test/
├── README.md
└── *.jmx                # JMeter test plans, e.g. Pmsrevamp_Test_Script.jmx
```

The main script (`Pmsrevamp_Test_Script.jmx`) is organized into numbered setup/preparation/execution thread groups (S01–S19), one per workflow: authentication partner self-registration, device provider self-registration, FTM provider self-registration, policy request, OIDC client, API key, device/SBI management, FTM chip management, certificate trust store (root/intermediate CA), and partner/policy admin actions. See `README.md` for the full thread-group-by-thread-group breakdown.

---

## 3. How to run

1. Download and install [Apache JMeter](https://jmeter.apache.org/download_jmeter.cgi).
2. Open the relevant `.jmx` script, validate it for a single user, then dry-run for ~10 minutes before a full load run.
3. `auth-demo-service` (from [mosip-functional-tests](https://github.com/mosip/mosip-functional-tests)) must be installed and reachable — the scripts use it to generate auth tokens, e.g.:
   ```shell
   java -Dmosip.base.url=https://api-internal.<env>.mosip.net -Dserver.port=8082 \
     -Dauth-token-generator.rest.clientId=mosip-resident-client \
     -Dauth-token-generator.rest.secretKey=<secret> -Dauth-token-generator.rest.appId=resident \
     -jar authentication-demo-service-<version>.jar
   ```
4. Workload sizing follows Little's Law: `Users = TPS * (SLA + think time + pacing)`. Use JMeter's Constant Throughput Timer (hits/min = TPS × 60) to control request rate.

---

## 4. Agent rules

### Do

1. Keep new thread groups numbered and named consistently with the existing `S<NN> <Workflow> (Preparation|Execution)` convention documented in `README.md`.
2. Validate a new/changed script with a single user and a short dry run before a full load run.
3. Update `README.md`'s thread-group list when adding or renaming a thread group.

### Do not

1. Point these scripts at a production environment without a change-managed load window — they generate real partner/policy/device/certificate records.
2. Assume `auth-demo-service` is already deployed in a target environment; it's a separate install from [mosip-functional-tests](https://github.com/mosip/mosip-functional-tests).

---

*Last updated: 2026-08-10.*
