package io.mosip.pms.partner.misp.dto;

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
	//misp license
	
	private List<String> postmisps;
	
	private List<String> putmisps;
	
	private List<String> getmisps;
	
	private List<String> getmispsmispidlicensekey;
	
}