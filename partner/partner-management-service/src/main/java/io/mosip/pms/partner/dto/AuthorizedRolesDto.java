package io.mosip.pms.partner.dto;

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
	//Partner service
	
	private List<String> postpartners;
    
    private List<String> patchpartnerspartneridapikeyrequest;

    private List<String> postpartnerspartneridbioextractorspolicyid;
    
    private List<String> getpartnerspartneridbioextractorspolicyid;
	
	private List<String> postpartnerspartneridcredentialtypepolicyid;
	
	private List<String> getpartnerspartneridcredentialtypepolicies;
	
	private List<String> postpartnerspartneridcontactadd;
	
	private List<String> putpartnerspartnerid;
	
	private List<String> getpartnerspartnerid;
	
	private List<String> getpartnerspartneridapikeyrequest;
	
	private List<String> getpartnerspartneridapikeyapikeyreqid;
	
	private List<String> postpartnerscertificatecaupload;
	
	private List<String> postpartnerscertificateupload;
	
	private List<String> getpartnerspartneridcertificate;
	
	private List<String> postpartnerssearch;
	
	private List<String> postpartnerspartnertypesearch;
	
	private List<String> postpartnersfiltervalues;
	
	private List<String> postpartnersapikeyrequestfiltervalues;
	
	private List<String> postpartnersapikeyrequestsearch;
	
	private List<String> postpartnersapikeysearch;
		
}