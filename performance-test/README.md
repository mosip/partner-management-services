
### Contains
* This folder contains performance test script of below API endpoint categories.
	01. Generate Auth Token For Keycloak User And Pms Client Secret Key (Setup)
	02. Generate Policy Group And Policies (Setup)
	03. Generate Authentication, Device Provider, FTM Provider And Partner Admin Partners (Setup)
	04. Generate Auth Token For Authentication, Device Provider, FTM Provider And Partner Admin Partners (Setup)
	05. S01 Register Partner (Execution)
	06. S02 User Consent (Execution)
	07. S03 Select Policy Group (Execution)
	08. S04 Partner Certificate (Execution)
	09. S05 Policy Request (Execution)
	10. S06 Authentication Services Oidc Client (Preparation)
	11. S06 Authentication Services Oidc Client (Execution)
	12. S07 Authentication Services API Key (Preparation)
	13. S07 Authentication Services API Key (Execution)
	14. S08 Device Provider Services (Preparation)
	15. S08 Device Provider Services (Execution)
	16. S09 FTM Chip Provider Service (Preparation)
	17. S09 FTM Chip Provider Service (Execution)
	18. S10 Partner Admin Certificate Trust Store Root CA (Preparation)
	19. S10 Partner Admin Certificate Trust Store Root CA (Execution)
	20. S11 Partner Admin Certificate Trust Store Intermediate CA (Preparation)
	21. S11 Partner Admin Certificate Trust Store Intermediate CA (Execution)
	22. S12 Partner Admin Partners (Preparation)
	23. S12 Partner Admin Partners (Execution)
	24. S13 Partner Admin Policies Policy Group (Execution)
	25. S14 Partner Admin Policies Authentication And Datashare Policy (Preparation)
	26. S14 Partner Admin Policies Authentication And Datashare Policy (Execution)
	27. S15 Partner Admin Partner Policy Linking (Preparation)
	28. S15 Partner Admin Partner Policy Linking (Execution)
	29. S16 Partner Admin SBI (Preparation)
	30. S16 Partner Admin SBI (Execution)
	31. S17 Partner Admin Device (Preparation)
	32. S17 Partner Admin Device (Execution)
	33. S18 Partner Admin FTM Device (Preparation)
	34. S18 Partner Admin FTM Device (Execution)
	35. S19 Partner Admin Authentication Services OIDC Client (Preparation)
	36. S19 Partner Admin Authentication Services OIDC Client (Execution)
	37. S20 Partner Admin Authentication Services API Key (Preparation)
	38. S20 Partner Admin Authentication Services API Key (Execution)
	
* Open source Tools used,
    1. [Apache JMeter](https://jmeter.apache.org/)

### How to run performance scripts using Apache JMeter tool
* Download Apache JMeter from https://jmeter.apache.org/download_jmeter.cgi
* Download scripts for the required module.
* Start JMeter by running the jmeter.bat file for Windows or jmeter file for Unix. 
* Validate the scripts for one user.
* Execute a dry run for 10 min.
* Execute performance run with various loads in order to achieve targeted NFR's.

### Procedure to install and execute auth-demo-service in the local machine

* The following link provides installation of auth-demo-service 
	* https://github.com/mosip/mosip-functional-tests/blob/master/README.md
	
* Navigate to the path where auth-demo-service has been installed and run following query for cellbox1 env.
	*java -jar -Dmosip.base.url=https://api-internal.cellbox1.mosip.net -Dserver.port=8082 -Dauth-token-generator.rest.clientId=mosip-resident-client -Dauth-token-generator.rest.secretKey=abc@123 -Dauth-token-generator.rest.appId=resident authentication-demo-service-1.2.1-develop-SNAPSHOT.jar

### Execution points for eSignet Authentication API's

*Pmsrevamp_Test_Script.jmx
	
	* Generate Auth Token For Keycloak User And Pms Client Secret Key (Setup) : This threadgroup generates the token for keycloak user (For Policy creation) and mosip-pms-client (To generate partner)
	* Generate Policy Group And Policies (Setup): This threadgroup creates 
	
	
### Downloading Plugin manager jar file for the purpose of installing other JMeter specific plugins

* Download JMeter plugin manager from below url links.
	*https://jmeter-plugins.org/get/

* After downloading the jar file place it in below folder path.
	*lib/ext

* Please refer to following link to download JMeter jars.
	https://mosip.atlassian.net/wiki/spaces/PT/pages/1227751491/Steps+to+set+up+the+local+system#PluginManager
		
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
