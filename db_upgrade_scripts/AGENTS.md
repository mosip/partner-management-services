# AGENTS.md — `db_upgrade_scripts/`

> Incremental upgrade/rollback SQL for an already-deployed `mosip_pms` database.
> Parent guide: [repo root `AGENTS.md`](../AGENTS.md).
> Related: [`db_scripts/AGENTS.md`](../db_scripts/AGENTS.md) (greenfield install).

---

## 1. Purpose

Use these scripts to move an **existing** `mosip_pms` database between released versions without dropping data. For a brand-new/empty database, use [`db_scripts/`](../db_scripts/AGENTS.md) instead.

---

## 2. Layout

```text
db_upgrade_scripts/
├── README.MD
└── mosip_pms/
    ├── upgrade.sh              # entry point, driven by upgrade.properties
    ├── upgrade.properties      # DB conn info, CURRENT_VERSION, UPGRADE_VERSION, ACTION
    └── sql/
        ├── {from}_to_{to}_upgrade.sql
        └── {from}_to_{to}_rollback.sql
```

Version chain currently spans `1.1.5.5` → `1.2.0.1-B1` → `1.2.0.1-B2` → `1.2.0.1-B3` → `1.2.0.1-B4` → `1.2.0.1` → `1.2.1.0` → `1.2.2.0` → `1.2.2.1` → `1.2.2.2` → `1.2.2.3` → `1.2.2.4` → `1.3.0`. Each hop has a matching `_upgrade.sql` and `_rollback.sql` pair, named exactly `{CURRENT_VERSION}_to_{UPGRADE_VERSION}_{upgrade|rollback}.sql`.

The one exception: the `1.1.5.5_to_1.2.0.1-B1` hop also runs two one-off support scripts (`..._pms-authdevice-support.sql`, `..._pms-regdevice-support.sql`) against separate `mosip_authdevice` / `mosip_regdevice` source databases before the main upgrade script — `upgrade.sh` special-cases this hop by version number, not by any general mechanism.

---

## 3. How to run

```bash
cd db_upgrade_scripts/mosip_pms
# edit upgrade.properties: DB_SERVERIP, DB_PORT, SU_USER, SU_USER_PWD, MOSIP_DB_NAME,
#   CURRENT_VERSION, UPGRADE_VERSION, ACTION=upgrade (or rollback)
./upgrade.sh upgrade.properties
```

`upgrade.sh` terminates active connections to `MOSIP_DB_NAME` first, then runs `sql/${CURRENT_VERSION}_to_${UPGRADE_VERSION}_upgrade.sql` (or `_rollback.sql` when `ACTION=rollback`). It exits with an error if the expected file for that version pair doesn't exist — it does not chain through intermediate versions automatically, so upgrading across multiple hops means running it once per hop in order.

---

## 4. Adding a new version hop

1. Add both `sql/{from}_to_{to}_upgrade.sql` and `sql/{from}_to_{to}_rollback.sql` — never ship one without the other.
2. Mirror the same schema change in [`db_scripts/mosip_pms/ddl/`](../db_scripts/AGENTS.md) so a fresh install ends up at the same schema as an upgraded one.
3. Update this file's version chain list above when a new released version is added.

---

## 5. Agent rules

### Do

1. Always add matching `_upgrade.sql` and `_rollback.sql` files for any production-bound schema change.
2. Name files exactly `{from}_to_{to}_upgrade.sql` / `_rollback.sql` — `upgrade.sh` builds the filename from `CURRENT_VERSION`/`UPGRADE_VERSION` and fails if it doesn't match.
3. Set `upgrade.properties` (connection info, versions, `ACTION`) before running `upgrade.sh`.
4. Keep `db_scripts/` (greenfield DDL) and this folder's cumulative upgrades consistent with each other.

### Do not

1. Skip the rollback script — `ACTION=rollback` will fail with "rollback script not found" if it's missing.
2. Assume `upgrade.sh` chains multiple version hops in one run — it only executes the single hop named by `CURRENT_VERSION`/`UPGRADE_VERSION`.
3. Reuse the `1.1.5.5_to_1.2.0.1-B1` special-cased support-script pattern for new hops without a genuine cross-database migration need.

---

*Last updated: 2026-08-10.*
