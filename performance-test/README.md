
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


### Execution points for eSignet Authentication API's

*esignet_mockida_test_script.jmx
	
	* Create OIDC Client in Mock Authentication System (Setup) : This threadgroup generates client id and encoded private key pair and stored in csv file. 
	* Create Identities in Mock Identity System (Setup) : This threadgroup generates mock identities and stores in mock identity database. These identities are used for authentication in eSignet portal.
    * S01 OTP authentication (Execution)
		*S01 T01 GetCsrf: This API endpoint generates CSRF token.
		*S01 T02 Oauthdetails : This API endpoint hits Oauthdetails endpoint of eSignet.
		*S01 T03 Send OTP : This API endpoint sends OTP request for authentication.
		*S01 T04 Authentication : This API endpoint performs authentication in eSignet portal
		*S01 T05 Authorization : This API endpoint performs authorization in eSignet portal
		*S01 T06 Token: Code created in the preparation will be used only once and a signed JWT key value is also required for which we are using a JSR223 Pre-processor. The Pre-processor(Generate Client Assertion) will generate a signed JWT token value using the client id and its private key from the file created in Create OIDC Client in Mock Authentication System (Setup). An access token will be generated in the response body.
		*S01 T07 Userinfo: For execution the generated access token from the token end point api is used. Till the token is not expired it can be used for multiple samples.
		
*esignet_mosipida_test_script.jmx
	
	* Create Identities in MOSIP Identity System (Setup) : This threadgroup generates VIDs and passwords for eSignet authentication and stored in csv file. 
	* Create OIDC Client in MOSIP Authentication System (Setup): This threadgroup generates client Id and encoded private key for eSignet authentication.
    * S01 OTP authentication (Execution)
		*S01 T01 Get Csrf Token: This API endpoint generates CSRF token.
		*S01 T02 Oauth Details : This API endpoint hits Oauthdetails endpoint of eSignet.
		*S01 T03 Send OTP : This API endpoint sends OTP request for authentication.
		*S01 T04 Authentication : This API endpoint performs OTP authentication in eSignet portal
		*S01 T05 Authorization Code : This API endpoint performs authorization in eSignet portal
		*S01 T06 Token: Code created in the preparation will be used only once and a signed JWT key value is also required for which we are using a JSR223 Pre-processor. The Pre-processor(Generate Client Assertion) will generate a signed JWT token value using the client id and its private key from the file created in Create OIDC Client in MOSIP Authentication System (Setup). An access token will be generated in the response body.
		*S01 T07 Userinfo: For execution the generated access token from the token end point api is used. Till the token is not expired it can be used for multiple samples.
	* S02 Password Authentication (Execution)
		*S02 T01 Get Csrf Token: This API endpoint generates CSRF token.
		*S02 T02 Oauth Details : This API endpoint hits Oauthdetails endpoint of eSignet.
		*S02 T03 Authentication : This API endpoint performs Password authentication in eSignet portal
		*S02 T04 Authorization Code : This API endpoint performs authorization in eSignet portal
		*S02 T05 Token: Code created in the preparation will be used only once and a signed JWT key value is also required for which we are using a JSR223 Pre-processor. The Pre-processor(Generate Client Assertion) will generate a signed JWT token value using the client id and its private key from the file created in Create OIDC Client in MOSIP Authentication System (Setup). An access token will be generated in the response body.
		*S02 T06 Userinfo: For execution the generated access token from the token end point api is used. Till the token is not expired it can be used for multiple samples.
	
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
