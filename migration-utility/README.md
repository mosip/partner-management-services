# Migration Utility

## Overview

This utility is used when Partner Management Services are moving from 1.1.5 to 1.2.0. This utility will migrate/publish all the data required for IDA from PMS to websub.

## Build & run 

Provide required configurations in application.properties file like data base connection configurations...

a. Build
    ```
    $ cd utility
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true


