
### Contains
* This folder contains performance test script of below API endpoint categories.
	01. Generate Auth Token For Pms Client (Setup)
	02. Generate Partner Admin, Device Provider And FTM Provider Partners (Setup)
	03. Generate Policy Group And Policies (Setup)
	04. Generate Authentication Partner for Policy Creation,Oidc Client Creation And Api Key Generation (Setup)
	05. Generate Auth Token For Authentication, Device Provider, FTM Provider And Partner Admin Partners (Setup)
	06. S01 Register Partner (Execution)
	07. S02 User Consent (Execution)
	08. S03 Select Policy Group (Execution)
	09. S04 Partner Certificate (Execution)
	10. S05 Policy Request (Execution)
	11. S06 Authentication Services Oidc Client (Preparation)
	12. S06 Authentication Services Oidc Client (Execution)
	13. S07 Authentication Services API Key (Preparation)
	14. S07 Authentication Services API Key (Execution)
	15. S08 Device Provider Services (Preparation)
	16. S08 Device Provider Services (Execution)
	17. S09 FTM Chip Provider Service (Preparation)
	18. S09 FTM Chip Provider Service (Execution)
	19. S10 Partner Admin Certificate Trust Store Root CA (Preparation)
	20. S10 Partner Admin Certificate Trust Store Root CA (Execution)
	21. S11 Partner Admin Certificate Trust Store Intermediate CA (Preparation)
	22. S11 Partner Admin Certificate Trust Store Intermediate CA (Execution)
	23. S12 Partner Admin Partners (Preparation)
	24. S12 Partner Admin Partners (Execution)
	25. S13 Partner Admin Policies Policy Group (Execution)
	26. S14 Partner Admin Policies Authentication And Datashare Policy (Preparation)
	27. S14 Partner Admin Policies Authentication And Datashare Policy (Execution)
	28. S15 Partner Admin Partner Policy Linking (Preparation)
	29. S15 Partner Admin Partner Policy Linking (Execution)
	30. S16 Partner Admin SBI (Preparation)
	31. S16 Partner Admin SBI (Execution)
	32. S17 Partner Admin Device (Preparation)
	33. S17 Partner Admin Device (Execution)
	34. S18 Partner Admin FTM Device (Preparation)
	35. S18 Partner Admin FTM Device (Execution)
	36. S19 Partner Admin Authentication Services OIDC Client (Preparation)
	37. S19 Partner Admin Authentication Services OIDC Client (Execution)
	38. S20 Partner Admin Authentication Services API Key (Preparation)
	39. S20 Partner Admin Authentication Services API Key (Execution)
	
* Open source Tools used,
    1. [Apache JMeter](https://jmeter.apache.org/)

### How to run performance scripts using Apache JMeter tool
* Download Apache JMeter from https://jmeter.apache.org/download_jmeter.cgi
* Download scripts for the required module.
* Start JMeter by running the jmeter.bat file for Windows or jmeter file for Unix. 
* Validate the scripts for one user.
* Execute a dry run for 10 min.
* Execute performance run with various loads in order to achieve targeted NFR's.
* Download JMeter plugin manager from below url links.
	*https://jmeter-plugins.org/get/
* After downloading the jar file place it in below folder path.
	*lib/ext
* Please refer to following link to download JMeter jars.
	https://www.blazemeter.com/blog/jmeter-plugins-manager

### Procedure to install and execute auth-demo-service in the local machine

* The following link provides installation of auth-demo-service 
	* https://github.com/mosip/mosip-functional-tests/blob/master/README.md
	
* Navigate to the path where auth-demo-service has been installed and run following query for cellbox1 env.
	*java -jar -Dmosip.base.url=https://api-internal.cellbox1.mosip.net -Dserver.port=8082 -Dauth-token-generator.rest.clientId=mosip-resident-client -Dauth-token-generator.rest.secretKey=abc@123 -Dauth-token-generator.rest.appId=resident authentication-demo-service-1.2.1-develop-SNAPSHOT.jar

### Execution points for eSignet Authentication API's

*Pmsrevamp_Test_Script.jmx
	
	* Generate Auth Token For Pms Client (Setup) : This threadgroup generates the token for mosip-pms-client (To generate partner).
	
	* Generate Partner Admin, Device Provider And FTM Provider Partners (Setup): This threadgroup creates Device Provider Partner, FTM Provider Partner, Partner Admin Partner.
	
	* Generate Policy Group And Policies (Setup): This threadgroup creates Policy Group with multiple policy names associated with it. The number of policies is defined by user-defined variable - numberOfPolicies
	
	* Generate Authentication Partner For Policy Request Creation, Oidc Client Creation And Api Key Generation (Setup) : This threadgroup creates Authentication Partner for policy creation, Authentication Partner for OIDC client creation and Authentication Partner for API key generation.
	
	* Generate Authentication Token For Authentication, Device Provider, FTM Provider And Partner Admin Partners (Setup); This threadgroup generates Authentication Token for Authentication Partner, Device Provider Partner, FTM Provider Partner, Partner Admin Partner, Authentication Partner for OIDC client creation and Authentication Partner for API key.
	
	*S01 Register Partner (Execution);
		* S01 T01 Self Register As A Partner: This API endpoint performs self registers for partner.
		* S01 T02 Partner Email Verification: This API endpoint performs email verification for the newly self registered partner.
		
	*S02 User Consent (Execution):
		* S02 T01 Post User Consent: This API endpoint performs user-consent with POST method.
		* S02 T02 Get User Consent: This API endpoint performs user-consent with POST method.
	
	*S03 Select Policy Group (Execution):
		*S03 T01 Select Policy Group: This API endpoint fetches the policy group.
	
	*S04 Partner Certificate (Execution):
		*S04 T01 Upload Partner Certificate: This API endpoint uploads the partner certificate.
		*S04 T02 Get Certificate Details: This API endpoint fetches the partner certificate details.
		*S04 T03 Get Certificate Data: This API endpoint fetches the partner certificate data.
	
	*S05 Policy Request (Execution):
		*S05 T01 Policy Group: This API endpoint fetches the policy group details.
		*S05 T02 Active Policy Group: This API endpoint fetches the active policy group details.
		*S05 T03 Create Policy Request:This API endpoint created new policy request.
		*S05 T04 List All Policy Requests: This API endpoint lists all policy requests.
		
	*S06 Authentication Services Oidc Client (Preparation): This threadgroup approves the policies for the OIDC client creation.
	
	*S06 Authentication Services Oidc Client (Execution):
		*S06 T01 Policy Group: This API endpoint fetches the policy group details.
		*S06 T02 List All Policy Requests: This API endpoint lists all policy requests.
		*S06 T03 Create Oidc Client: This API endpoint creates OIDC client Id.
		*S06 T04 List All Oauths: This API endpoint lists all OIDC client Ids.
		*S06 T05 Update Oidc Client: This API endpoint updates OIDC client details.
		
	* S07 Authentication Services API Key (Preparation): This threadgroup approves the policies for Api key generation.
	
	* S07 Authentication Services API Key (Execution):
		*S07 T01 Policy Group: This API endpoint fetches the policy group details.
		*S07 T02 List All Policy Requests: This API endpoint lists all policy requests.
		*S07 T03 Generate Api Key: This API endpoint generated Api key.
		*S07 T04 List All Api Keys: This API endpoint lists all Api keys.
		*S07 T05 Deactivate Api Key: This API endpoint deactivate Api key.
		
	*S08 Device Provider Services (Preparation): This threadgroup creates Secure Biometric Interface Id for Deactivate Sbi Ids usecase and for usecase create Device Ids. Also generate approved device Ids to deactivate device usecase.
	
	*S08 Device Provider Services (Execution):
		*S08 T01 Device Provider Status: This API endpoint fetches Device Provider partner details.
		*S08 T02 Create Secure Biometric Interface: This API endpoint creates Secure Biometric Interface Id.
		*S08 T03 List All Sbis: This API endpoint lists all Secure Biometric Interface Ids.
		*S08 T04 Deactivate Sbi: This API endpoint deactivates Secure Biometric Interface Id.
		*S08 T05 Device Type: This API endpoint creates Device type table.
		*S08 T06 Device Subtype: This API endpoint creates Device subtype table.
		*S08 T07 Add Device: This API endpoint creates device Id.
		*S08 T08 List All Devices With SbId: This API endpoint list all device Ids with specific sbi id.
		*S08 T09 Deactivate Device: This API endpoint deactivates the device id.
		
	*S09 FTM Chip Provider Service (Preparation): This threadgroup  generates approved Ftp Ids for the deactivate Ftp Id usecase.
	
	*S09 FTM Chip Provider Service (Execution):
		*S09 T01 Ftm Provider Status: This API endpoint fetches FTM Provider partner details.
		*S09 T02 Create Ftp Chip Details: This API endpoint creates Ftp chip details.
		*S09 T03 Upload Certificate: This API endpoint uploads FTM provider partner certificate.
		*S09 T04 List All Ftm Chip Details: This API endpoint lists all Ftm chip details.
		*S09 T05 Certificate Data: This API endpoint fetches certificate of Ftm chip.
		*S09 T06 Deactivate Ftp Chip: This API endpoint deactivates Ftm chip.
		
	*S10 Partner Admin Certificate Trust Store Root CA (Preparation): This threadgroup  generates Root CA certificate.
	
	*S10 Partner Admin Certificate Trust Store Root CA (Execution):
		*S10 T01 Upload Certificate: This API endpoint uploads Root Ca certificate.
		*S10 T02 List All Certificates: This API endpoint lists all Root Ca certificates.
		*S10 T03 Download Root Certificate: This API endpoint downloads Root Ca certificate.
		
	*S11 Partner Admin Certificate Trust Store Intermediate CA (Preparation): This threadgroup  generates Intermediate certificates.
	
	*S11 Partner Admin Certificate Trust Store Intermediate CA (Execution):
		*S11 T01 Upload Intermediate Certificate: This API endpoint uploads Intermediate certificate.
		*S11 T02 List All Certificates: This API endpoint lists all Intermediate certificates.
		*S11 T03 Download Intermediate Certificate: This API endpoint downloads intermediate certificate.
		
	*S12 Partner Admin Partners (Preparation): This threadgroup  generates Partner Ids for deactivate partners usecase.
	
	*S12 Partner Admin Partners (Execution):
		*S12 T01 View All Partners: This API endpoint lists all partners.
		*S12 T02 Details Of Partner: This API endpoint shows the details of a partner.
		*S12 T03 Download Certificate: This API endpoint downloads the certificate.
		*S12 T04 Deactivate Partner: This API endpoint deactivates the partner.
		
	*S13 Partner Admin Policies Policy Group (Execution): 
		*S13 T01 Create New Policy Group: This API endpoint creates new policy group.
		*S13 T02 List All Policy Groups: This API endpoint lists all policy groups.
		*S13 T03 Deactivate Policy Group: This API endpoint deactivates policy group.
		*S13 T04 Get Policy Group: This API endpoint fetches policy group.
		
	*S14 Partner Admin Policies Authentication And Datashare Policy (Preparation): This threadgroup  creates new policy for update policy usecase.
	
	*S14 Partner Admin Policies Authentication And Datashare Policy (Execution):
		*S14 T01 Create New Policy: This API endpoint creates new policy.
		*S14 T02 List All Policies: This API endpoint lists all policy.
		*S14 T03 View Policy: This API endpoint fetches details of the policy.
		*S14 T04 Publish Policy: This API endpoint publishes the policy.
		*S14 T05 Update Policy: This API endpoint updates the policy.
		*S14 T06 Deactivate Policy: This API endpoint deactivates the policy.
		
	*S15 Partner Admin Partner Policy Linking (Preparation): This threadgroup generates mapping key to approve partner policy.
	
	*S15 Partner Admin Partner Policy Linking (Execution):
		*S15 T01 List All Policies: This API endpoint lists all policies.
		*S15 T02 Approve Partner: This API endpoint approves partner policy.
		
	*S16 Partner Admin SBI (Preparation):This threadgroup generates secure biometric interface id to approved and deactivated by Partner Admin.
	
	*S16 Partner Admin SBI (Execution):
		*S16 T01 List All Sbi Ids: This API endpoint lists all secure biometric interface ids.
		*S16 T02 Approve Biometric Interface: This API endpoint approves secure biometric interface id.
		*S16 T03 Deactivate Sbi: This API endpoint deactivates secure biometric interface id.
		*S16 T04 List Of Devices: This API endpoint lists the devices of sbids.
	
	*S17 Partner Admin Device (Preparation): This threadgroup generates device ids to approved and deactivated by Partner Admin.
		
	*S17 Partner Admin Device (Execution):
		*S17 T01 List All Device: This API endpoint lists all device ids.
		*S17 T02 Approve Device: This API endpoint approves device id.
		*S17 T03 Deactivate Device: This API endpoint deactivates the device id.
		
	*S18 Partner Admin FTM Device (Preparation): This threadgroup generates Ftp chip ids to approved and deactivated by Partner Admin.
	
	*S18 Partner Admin FTM Device (Execution):
		*S18 T01 List All Ftm: This API endpoint lists all ftp chip ids.
		*S18 T02 Download Certificate: This API endpoint downloads the certificate data.
		*S18 T03 Approve Ftp Chip: This API endpoint approves ftp chip id.
		*S18 T04 Deactivate Ftp Chip: This API endpoint deactivates ftp chip id.
		
	*S19 Partner Admin Authentication Services OIDC Client (Preparation): This threadgroup generates Oidc client id to be fetched and deactivated by Partner Admin.
	
	*S19 Partner Admin Authentication Services OIDC Client (Execution):
		*S19 T01 List All Oauths: This API endpoint lists all oauths.
		*S19 T02 View Oidc Client Details: This API endpoint fetches oidc client details.
		*S19 T03 Deactivate Oidc Client: This API endpoint deactivates oidc client id.
		
	*S20 Partner Admin Authentication Services API Key (Preparation): This threadgroup generates api key to be deactivated by Partner Admin.
	
	*S20 Partner Admin Authentication Services API Key (Execution):
		*S20 T01 List All Api Keys: This API endpoint lists all Api keys.
		*S20 T02 Deactivate Api Key: This API endpoint deactivates Api keys.
	
### Designing the workload model for performance test execution
* Calculation of number of users depending on Transactions per second (TPS) provided by client

* Applying little's law
	* Users = TPS * (SLA of transaction + think time + pacing)
	* TPS --> Transaction per second.
	
* For the realistic approach we can keep (Think time + Pacing) = 1 second for API testing
	* Calculating number of users for 10 TPS
		* Users= 10 X (SLA of transaction + 1)
		       = 10 X (1 + 1)
			   = 20
			   
### Usage of Constant Throughput timer to control Hits/sec from JMeter
* In order to control hits/ minute in JMeter, it is better to use Timer called Constant Throughput Timer.

* If we are performing load test with 10TPS as hits / sec in one thread group. Then we need to provide value hits / minute as in Constant Throughput Timer
	* Value = 10 X 60
			= 600

* Dropdown option in Constant Throughput Timer
	* Calculate Throughput based on as = All active threads in current thread group
		* If we are performing load test with 10TPS as hits / sec in one thread group. Then we need to provide value hits / minute as in Constant Throughput Timer
	 			Value = 10 X 60
					  = 600
		  
	* Calculate Throughput based on as = this thread
		* If we are performing scalability testing we need to calculate throughput for 10 TPS as 
          Value = (10 * 60 )/(Number of users)
