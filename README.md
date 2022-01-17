# Partner-management-services

This repository contains the source code and design documents for MOSIP partner management. For an overview refer [here](https://nayakrounak.gitbook.io/mosip-docs/modules/partner-management)

Partnermanagement module contains following services:
1. partner management service
2. Policy management service

# Database
 See [DB guide](db_scripts/README.md)
 
# Build
The project requires JDK 1.11. 
1. To build jars:
    ```
    $ cd partner
    $ mvn clean install 
    ```
1. To skip JUnit tests and Java Docs:
    ```
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true
    ```
1. To build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```

# Deploy

## PMS in Sandbix
To deploy PMS on Kubernetes cluster using Dockers refer to [mosip-infra](https://github.com/mosip/mosip-infra/tree/1.2.0-rc2/deployment/v3)

## Developer

1. As a developer, to run a service jar individually:
    ```
    `java -Dspring.profiles.active=<profile> -Dspring.cloud.config.uri=<config-url> -Dspring.cloud.config.label=<config-label> -jar <jar-name>.jar`
    ```
    Example:  
        _profile_: `env` (extension used on configuration property files*)    
        _config_label_: `master` (git branch of config repo*)  
        _config-url_: `http://localhost:51000` (Url of the config server*)  
	
1. Note that you will have to run the dependent services like kernel-config-server to run any service successfully.

# Dependencies

Partner management module dependends on following services.
 
 1. kernel-keymanager-service
 2. kernel-notification-service
 3. kernel-auth-service
 4. datashare service
 5. websub 
 
# Configuration
 
Refer to the [configuration guide](docs/configuration.md).

# Test
Automated functaionl tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests)

# APIs
API documentation available on Wiki: [Partner-management APIs]

# License
This project is licensed under the terms of [Mozilla Public License 2.0](https://github.com/mosip/mosip-platform/blob/master/LICENSE)
