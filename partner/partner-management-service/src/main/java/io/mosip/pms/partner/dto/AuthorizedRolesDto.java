package io.mosip.pms.partner.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.pms")
@Getter
@Setter
public class AuthorizedRolesDto {

	//OIDC Client Controller
	private List<String> postoidcclient;

	private List<String> putoidcclient;

	private List<String> getoidcclient;

	//Device Detail controller
	private List<String> postdevicedetail;
	
	private List<String> putdevicedetail;
	
	private List<String> patchdevicedetail;
	
	private List<String> postdevicedetailsearch;
	
	private List<String> postdevicedetaildevicetypesearch;
	
	private List<String> postdevicedetailfiltervalues;
	
	private List<String> postdevicedetaildevicetypefiltervalues;
	
	private List<String> postdevicedetaildevicesubtypefiltervalues;
	
	//FTPChipDetail Controller
	
	private List<String> postftpchipdetail;
	
	private List<String> putftpchipdetail;
	
	private List<String> patchftpchipdetail;
	
	private List<String> postftpchipdetailuploadcertificate;
	
	private List<String> getftpchipdetailgetpartnercertificate;
	
	private List<String> postftpchipdetailsearch;

	private List<String> getpartnersftmchipdetails;

	private List<String> getallsbidetails;

	//Secure Biometric Interface controller
	
	private List<String> postsecurebiometricinterface;
	
	private List<String> putsecurebiometricinterface;
	
	private List<String> patchsecurebiometricinterface;
	
	private List<String> postsecurebiometricinterfacesearch;
	
	private List<String> putsecurebiometricinterfacedevicedetailsmap;
	
	private List<String> putsecurebiometricinterfacedevicedetailsmapremove;
	
	private List<String> postsecurebiometricinterfacedevicedetailsmapsearch;
	
	private List<String> postsecurebiometricinterfacefiltervalues;
	
	
	//partner controller

	private List<String> postpartnersbioextractors;
	
	private List<String> getpartnersbioextractors;
	
	private List<String> postpartnerscredentialtypepolicies;
	
	private List<String> getpartnerscredentialtypepolicies;
	
	private List<String> postpartnerscontactadd;
	
	private List<String> putpartners;
	
	private List<String> putpartnersnew;
	
	private List<String> getpartnerspartnerid;
	
	private List<String> getpartnersapikeyrequest;
	
	private List<String> postpartnerscacertificateupload;
	
	private List<String> postpartnerscertificateupload;
	
	private List<String> getpartnerscertificate;
	
	private List<String> postpartnerssearch;
	
	private List<String> postpartnerspartnertypesearch;
	
	private List<String> postpartnersfiltervalues;
	
	private List<String> postpartnersapikeyrequestfiltervalues;
	
	private List<String> postpartnersapikeyrequestsearch;
	
	private List<String> postpartnersapikeysearch;
	
	private List<String> putpartnerspolicygroup;
	
	private List<String> postpartnerspolicymap;
	
	private List<String> patchpartnersgenerateapikey;
	
	//partner manager controller
	
	private List<String> putpartnersapikeypolicies;
	
	private List<String> patchpartners;
	
	private List<String> getpartners;

	private List<String> getpartnersnew;
	
	private List<String> getpartnersapikey;
	
	private List<String> putpartnerspolicymapping;
	
	private List<String> patchpartnerspolicyapikeystatus;

	private List<String> getadminpartners;
	
	//MISP License controller
	
	private List<String> postmisplicense;
	
	private List<String> putmisplicense;
	
	private List<String> getmisplicense;
	
	private List<String> getmisplicensekey;
	
	private List<String> postmispfiltervalues;
	
	private List<String> postmispsearch;

	//Multi Partner Service Controller

	private List<String> getpartnercertificates;

	private List<String> getapikeysforauthpartners;

	private List<String> getoauthclients;

	private List<String> userconsent;

	private List<String> getsbidetails;

	private List<String> postadddevicetosbi;

	private List<String> postdevicewithsbimapping;

	private List<String> patchdeactivatedevice;

	private List<String> patchdeactivatesbi;

	private List<String> getftmchipdetails;

	private List<String> patchdeactivateftm;

	private List<String> getoriginalftmcertificate;

	private List<String> getpartnerdetails;

	private List<String> getallpartnerpolicymappingrequests;

	private List<String> getoauthpartnersclients;

	private List<String> getpartnersapikeyrequests;

	private List<String> getalldevicedetails;

	private List<String> gettrustcertificates;

	private List<String> getdownloadtrustcertificates;

	private List<String> getpartnersv3;
		
}
