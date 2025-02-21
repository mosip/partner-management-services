# Partner Management Services (PMS)
[![Maven Package upon a push](https://github.com/mosip/partner-management-services/actions/workflows/push_trigger.yml/badge.svg?branch=release-1.3.x)](https://github.com/mosip/partner-management-services/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=release-1.3.x&project=mosip_partner-management-services&metric=alert_status)](https://sonarcloud.io/dashboard?branch=release-1.3.x&id=mosip_partner-management-services)

## Overview
This repository contains the source code MOSIP Partner Management module. For an overview refer [here](https://docs.mosip.io/1.2.0/modules/partner-management-services).  The module exposes API endpoints. For a reference front-end UI implementation refer to PMS Revamp UI [Partner-management UI github repo](https://github.com/mosip/partner-management-portal/tree/release-1.3.x/pmp-reactjs-ui)

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

## Deployment in K8 cluster with other MOSIP services:
### Pre-requisites
* Set KUBECONFIG variable to point to existing K8 cluster kubeconfig file:
  * ```
    export KUBECONFIG=~/.kube/<my-cluster.config>
    ```
### Install
  ```
    $ cd deploy
    $ ./install.sh
   ```
### Delete
  ```
    $ cd deploy
    $ ./delete.sh
   ```
### Restart
  ```
    $ cd deploy
    $ ./restart.sh
   ```

## Config
Add below property to ```partner-management-default.properties``` file in mosip-config repository to Deploy PMS Revamp 1.3.0-DP.1 release in your env.
```
## This property is used by kernel-authcodeflowproxy-api to check request is coming from allowed urls not.
auth.allowed.urls=https://${mosip.pmp.host}/,https://${mosip.pmp.reactjs.ui.host}/
```

## Test
Automated functional tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE)
