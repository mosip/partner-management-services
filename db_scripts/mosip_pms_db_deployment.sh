### -- ---------------------------------------------------------------------------------------------------------
### -- Script Name		: MOSIP ALL DB Artifacts deployment for Partner Management Service Module
### -- Deploy Module 	: MOSIP Partner Management Service Module
### -- Purpose    		: To deploy MOSIP Partner Management Service Module Database DB Artifacts.       
### -- Create By   		: Sadanandegowda DM
### -- Created Date		: Aug-2020
### -- 
### -- Modified Date        Modified By         Comments / Remarks
### -- -----------------------------------------------------------------------------------------------------------

#! bin/bash
echo "`date` : You logged on to DB deplyment server as : `whoami`"
echo "`date` : MOSIP Database objects deployment started...."

echo "=============================================================================================================="
bash ./mosip_pms/mosip_pms_db_deploy.sh ./mosip_pms/mosip_pms_deploy.properties
echo "=============================================================================================================="

echo "`date` : MOSIP DB Deployment for Partner Management Service databases is completed, Please check the logs at respective logs directory for more information"
 
