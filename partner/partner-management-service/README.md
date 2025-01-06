# Partner management service

## Overview
This repository contains the source code for partner management service. For an overview refer [here](https://docs.mosip.io/1.2.0/partners).

## Databases
Refer to [SQL scripts](../../db_scripts).

## Build & run (for developers)
The project requires JDK 21.0.3
and mvn version - 3.9.6

1. Build and install:
    ```
    $ cd partner
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```
2. Build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```
   
### Remove the version-specific suffix (PostgreSQL95Dialect) from the Hibernate dialect configuration
```
mosip.datasource.authdevice.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
mosip.datasource.regdevice.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### For Spring-boot 3.x we need to specify the ANT Path Matcher for using the existing ANT path patterns.
spring.mvc.pathmatch.matching-strategy=ANT_PATH_MATCHER

### Spring boot 3.x onwards we need to specify the below property to unmask values in actuator env url
management.endpoint.env.show-values=ALWAYS

### Configuration
[partner-management-default.properties](https://github.com/mosip/mosip-config/blob/master/partner-management-default.properties)

[application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties)

defined here.

### Add auth-adapter in a class-path to run a services
   ```
   <dependency>
       <groupId>io.mosip.kernel</groupId>
       <artifactId>kernel-auth-adapter</artifactId>
       <version>${kernel.auth.adapter.version}</version>
   </dependency>
   ```

## Deployment in K8 cluster with other MOSIP services:
### Pre-requisites
* Set KUBECONFIG variable to point to existing K8 cluster kubeconfig file:
    ```
    export KUBECONFIG=~/.kube/<k8s-cluster.config>
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

## Test
Automated functional tests available in [Functional Tests](../../api-test).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](../../LICENSE)