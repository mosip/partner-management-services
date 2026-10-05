-- Rollback not required for the default policy validity extension (MOSIP-42249).
-- The original hardcoded end dates are not restored; a 200 year validity is harmless on 1.2.2.2.
-- The mpolicy-default-mobile JSON correction (MOSIP-42571) is not reverted: the old value was invalid JSON.
\echo 'Rollback Queries not required for transition from $CURRENT_VERSION to $UPGRADE_VERSION'
