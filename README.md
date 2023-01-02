# Partner Management Services (PMS)
[![Maven Package upon a push](https://github.com/mosip/partner-management-services/actions/workflows/push_trigger.yml/badge.svg?branch=release-1.2.0.1)](https://github.com/mosip/partner-management-services/actions/workflows/push_trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=release-1.2.0.1&project=mosip_partner-management-services&metric=alert_status)](https://sonarcloud.io/dashboard?branch=release-1.2.0.1&id=mosip_partner-management-services)

## Overview
This repository contains the source code MOSIP Partner Management module. For an overview refer [here](https://docs.mosip.io/1.2.0/modules/partner-management-services).  The module exposes API endpoints. For a reference front-end UI implementation refer to [Partner-management UI github repo](https://github.com/mosip/partner-management-portal/)

Partnermanagement module contains following services:
1. Partner management service
2. Policy management service

## Databases
Refer to [SQL scripts](db_scripts).

## Build & run (for developers)
The project requires JDK 1.11. 
1. Build and install:
    ```
    $ cd kernel
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```
1. Build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```

## Deploy
To deploy PMS on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deployment/sandbox-deployment).

## Test
Automated functional tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE)
