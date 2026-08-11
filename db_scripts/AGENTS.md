# AGENTS.md — `db_scripts/`

> Greenfield PostgreSQL install for the `mosip_pms` database.
> Parent guide: [repo root `AGENTS.md`](../AGENTS.md).
> Related: [`db_upgrade_scripts/AGENTS.md`](../db_upgrade_scripts/AGENTS.md).

---

## 1. Purpose

Use this folder for **fresh** environments only (sandbox init, empty Postgres). Do **not** use `deploy.sh` to alter an existing database that must retain data — it drops the database and role first. For an existing deployment, use [`db_upgrade_scripts/`](../db_upgrade_scripts/AGENTS.md) instead.

Used automatically as part of [MOSIP Sandbox](https://docs.mosip.io/1.2.0/readme/technology/sandbox-details) DB initialization.

---

## 2. Layout

```text
db_scripts/
├── README.md
└── mosip_pms/
    ├── deploy.sh              # entry point: drop -> role -> DB -> DDL -> grants -> optional DML
    ├── deploy.properties      # DB_SERVERIP, DB_PORT, MOSIP_DB_NAME, DB_UNAME, DML_FLAG
    ├── db.sql                 # CREATE DATABASE
    ├── ddl.sql                # \ir includes of ddl/*.sql
    ├── ddl/                   # per-table DDL (pms-partner.sql, pms-auth_policy.sql, pms-misp.sql, ...)
    ├── dml/                   # seed data CSVs (loaded only when DML_FLAG=1)
    ├── dml.sql                # loads dml/*.csv
    ├── role_dbuser.sql        # creates the app DB user (pmsuser)
    ├── grants.sql             # grants for the app DB user
    ├── drop_db.sql            # destructive reset (used by deploy.sh)
    └── drop_role.sql          # destructive reset (used by deploy.sh)
```

There is a single schema, `mosip_pms`.

---

## 3. How to run

```bash
cd db_scripts/mosip_pms
# edit deploy.properties: DB_SERVERIP, DB_PORT, MOSIP_DB_NAME (default mosip_pms), DB_UNAME (default pmsuser), DML_FLAG
export SU_USER_PWD=<postgres-superuser-password>
export DBUSER_PWD=<app-db-user-password>
./deploy.sh deploy.properties
```

`deploy.sh` terminates active connections to `MOSIP_DB_NAME`, then **drops** the existing database and role before recreating them — never point it at a shared environment that must keep its data.

---

## 4. Adding schema changes

1. Add/edit DDL under `ddl/` (one file per table or FK set — see `pms-fk.sql` for cross-table foreign keys).
2. Wire the new file into `ddl.sql` via `\ir ddl/<file>.sql`.
3. If the change ships to an already-deployed environment, add a matching upgrade (and rollback) pair in [`db_upgrade_scripts/`](../db_upgrade_scripts/AGENTS.md) — this folder alone is not sufficient for upgrading a live deployment.
4. If default/reference data changed, add or update the matching CSV under `dml/` and keep `dml.sql` in sync; this only applies when `DML_FLAG=1`.

---

## 5. Agent rules

### Do

1. Keep new tables under `ddl/` **and** wire them into `ddl.sql` — a file that exists but isn't `\ir`-included is never applied.
2. Set `deploy.properties` and the `SU_USER_PWD` / `DBUSER_PWD` env vars before running `deploy.sh`.
3. Pair every schema change here with a corresponding entry in `db_upgrade_scripts/` for upgrading existing deployments.
4. Keep seed data in `dml/` CSVs, not inline in DDL files.

### Do not

1. Run `deploy.sh` against a database that must retain data — it drops the DB and role unconditionally.
2. Assume other MOSIP modules share this schema; `mosip_pms` is PMS-only.
3. Skip the `db_upgrade_scripts/` companion when a DDL change needs to reach an already-running environment.

---

*Last updated: 2026-08-10.*
