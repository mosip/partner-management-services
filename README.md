# Partner-management-services

This repository contains the source code and design documents for MOSIP partner management. For an overview refer [here](https://nayakrounak.gitbook.io/mosip-docs/modules/partner-management).  The module exposes API endpoints. For a reference front-end UI implementation refer to [Partner-management UI  github repo](https://github.com/mosip/partner-management-portal/tree/1.2.0-rc2).

Partnermanagement module contains following services:
1. Partner management service
2. Policy management service

# Database
 See [DB guide](db_scripts/README.md)
 
# Build & run (for developers)

## Prerequisites
### Java 
    JDK 1.11.
### Database
    Should able to connect to 'mosip_pms' database.(either local or hosted)
    
### Dependent Services
    Make sure that following services are running.(eithe local or hosted)
    1. kernel-auth-service
    2. kernel-keymanager-service
    3. kerenel-audit-service
    4. datashare service
    
### Configurations
    Copy the following configurations from 'mosip_config' repo of respective branch.
    1. application-default.properties
    2. partner-management-default.properties
    
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
1. To run a service jar individually

    ```
    `java -Dspring.profiles.active=<profile> -Dspring.cloud.config.uri=<config-url> -Dspring.cloud.config.label=<config-label> -jar <jar-name>.jar`
    ```
    Example:  
        _profile_: `env` (extension used on configuration property files*)    
        _config_label_: `master` (git branch of config repo*)  
        _config-url_: `http://localhost:51000` (Url of the config server*)  


# Deploy

## PMS in Sandbox
To deploy PMS on Kubernetes cluster using Dockers refer to [mosip-infra](https://github.com/mosip/mosip-infra/tree/1.2.0-rc2/deployment/v3)
 
# Configuration
 
Refer to the [configuration guide](docs/configuration.md).

# Test
Automated functaionl tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests)

# APIs
API documentation available on Wiki: [Partner-management APIs]

# License
This project is licensed under the terms of [Mozilla Public License 2.0](https://github.com/mosip/mosip-platform/blob/master/LICENSE)
