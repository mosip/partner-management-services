#!/bin/bash

set -e
SOURCE_DB1_NAME=mosip_authdevice
SOURCE_DB2_NAME=mosip_regdevice
SOURCE_DB1_SUPPORT_FILE=sql/1.1.5.5_to_1.2.0.1-B1_pms-authdevice-support.sql
SOURCE_DB2_SUPPORT_FILE=sql/1.1.5.5_to_1.2.0.1-B1_pms-regdevice-support.sql
properties_file="$1"
echo `date "+%m/%d/%Y %H:%M:%S"` ": $properties_file"
if [ -f "$properties_file" ];
then
     echo `date "+%m/%d/%Y %H:%M:%S"` ": Property file \"$properties_file\" found."
    while IFS='=' read -r key value || [ -n "$key" ];
    do
        # Trim spaces
        key=$(echo "$key" | tr -d ' ')
        value=$(echo "$value" | xargs) # Trim spaces around the value
        
        # Set the variable dynamically
        eval "${key}=\"${value}\""
    done < "$properties_file"
else
     echo `date "+%m/%d/%Y %H:%M:%S"` ": Property file not found, Pass property file name as argument."
fi

echo "Current version: $CURRENT_VERSION"
echo "UPGRADE version: $UPGRADE_VERSION"
echo "Action: $ACTION"

# Terminate existing connections
echo "Terminating active connections"
CONN=$(PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$DEFAULT_DB_NAME -t -c "SELECT count(pg_terminate_backend(pg_stat_activity.pid)) FROM pg_stat_activity WHERE datname = '$MOSIP_DB_NAME' AND pid <> pg_backend_pid()";exit;)
echo "Terminated connections"

# Execute upgrade or rollback
if [ "$ACTION" == "upgrade" ]; then
  echo "Upgrading database from $CURRENT_VERSION to $UPGRADE_VERSION"
  UPGRADE_SCRIPT_FILE="sql/${CURRENT_VERSION}_to_${UPGRADE_VERSION}_upgrade.sql"
  if [ -f "$UPGRADE_SCRIPT_FILE" ]; then
    echo "Executing upgrade script $UPGRADE_SCRIPT_FILE"
    if [[ "$UPGRADE_VERSION" == "1.2.0.1-B1"  &&  "$CURRENT_VERSION" == "1.1.5.5" ]]; then
    		echo "Creating dml directory."
    		mkdir dml
		PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$SOURCE_DB1_NAME -a -b -f $SOURCE_DB1_SUPPORT_FILE
		PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$SOURCE_DB2_NAME -a -b -f $SOURCE_DB2_SUPPORT_FILE
	fi
    PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$DEFAULT_DB_NAME -a -b -f $UPGRADE_SCRIPT_FILE
  else
    echo "Upgrade script not found, exiting."
    exit 1
  fi
elif [ "$ACTION" == "rollback" ]; then
  echo "Rolling back database for $CURRENT_VERSION to $UPGRADE_VERSION"
  REVOKE_SCRIPT_FILE="sql/${CURRENT_VERSION}_to_${UPGRADE_VERSION}_rollback.sql"
  if [ -f "$REVOKE_SCRIPT_FILE" ]; then
    echo "Executing rollback script $REVOKE_SCRIPT_FILE"
    PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$DEFAULT_DB_NAME -a -b -f $REVOKE_SCRIPT_FILE
  else
    echo "rollback script not found, exiting."
    exit 1
  fi
else
  echo "Unknown action: $ACTION, must be 'upgrade' or 'rollback'."
  exit 1
fi
