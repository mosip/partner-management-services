# partner-management-services

This repository contains the source code and design documents for MOSIP partner management. To know more about MOSIP, its architecture, external integrations, releases, etc., please check the [Platform Documentation](https://github.com/mosip/mosip-docs/wiki)

### Dependencies
Partner services dependencies are mentioned below.  For all Kernel services refer to [commons repo](https://github.com/mosip/commons)  
* misp-service
  * kernel-idgenerator-mispid -- for misp id generation
  * kernel-licensekeygenerator-misp -- for misp license key generation
### Roles
	* MISP
	* PolicyManager
	* PartnerManager
	* Partner

### Build
The following commands should be run in the parent project to build all the modules - 
`mvn clean install`
The above command can be used to build individual modules when run in their respective folders

### Deploy
The following command should be executed to run any service locally in specific profile and local configurations - 
`java -Dspring.profiles.active=<profile> -jar <jar-name>.jar`

The following command should be executed to run any service locally in specific profile and `remote` configurations - 
`java -Dspring.profiles.active=<profile> -Dspring.cloud.config.uri=<config-url> -Dspring.cloud.config.label=<config-label> -jar <jar-name>.jar`

The following command should be executed to run a docker image - 
`docker run -it -p <host-port>:<container-port> -e active_profile_env={profile} -e spring_config_label_env= {branch} -e spring_config_url_env={config_server_url} <docker-registry-IP:docker-registry-port/<dcker-image>`

### Configurations
All the configurations used by the codebase in `mosip-platform` is present in [mosip-config](https://github.com/mosip/mosip-config) repository.

### Functional Test-cases
Functional tests run against the codebase in `mosip-platform` is present in [mosip-functional-tests](https://github.com/mosip/mosip-functional-tests) repository.

### Documentation
Relevant documents to get started with MOSIP can be found in [mosip-docs](https://github.com/mosip/mosip-docs) repository. 
In order to get started, please refer to the [Getting-Started](https://github.com/mosip/mosip-docs/wiki/Getting-Started) guide.

### Infra
Automated scripts to build and deploy MOSIP modules are present in [mosip-infra](https://github.com/mosip/mosip-infra) repository.


---

### Contribute
You can contribute to MOSIP! 

We want to engage constructively with the community.  If you find a **vulnerability** or issue, please file a bug with the respective repository.  We welcome pull requests with fixes too.  Please see the [Contributor Guide](https://github.com/mosip/mosip-docs/wiki/Contributor-Guide) on how to file bugs, contribute code, and more.

### License
This project is licensed under the terms of [Mozilla Public License 2.0](https://github.com/mosip/mosip-platform/blob/master/LICENSE)

### Communication
Join the [developer mailing list](https://groups.io/g/mosip-dev)


You may also be interested in joining our community room on Gitter via [![Gitter](https://badges.gitter.im/mosip-community/community.svg)](https://gitter.im/mosip-community/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)  where you could get some great community support
