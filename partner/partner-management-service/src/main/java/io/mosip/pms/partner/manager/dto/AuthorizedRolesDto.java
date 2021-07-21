package io.mosip.pms.partner.manager.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.pms")
@Getter
@Setter
public class AuthorizedRolesDto {
	//Partner management
	
	private List<String> putpartnerspartneridapikeypolicies;
	
	private List<String> patchpartnerspartnerid;
	
	private List<String> patchpartnerspartneridapikey;
	
	private List<String> getpartnerspartneridapikeymisplicensekey;
	
	private List<String> patchpartnersapikey;
	
	private List<String> getpartners;
	
	private List<String> getpartnerspartneridapikey;
	
	private List<String> getpartnersapikey;
	
	private List<String> getpartnersapikeykey;
		
}