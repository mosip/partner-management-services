package io.mosip.pms.device.dto;

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
//device Detail
    private List<String> postdevicedetail;
    
    private List<String> putdevicedetail;
	
	private List<String> patchdevicedetail;
    
    private List<String> postdevicedetailsearch;

    private List<String> postdevicedetaildevicetypesearch;
     
    private List<String> postdevicedetailfiltervalues;
	
	private List<String> postdevicedetaildevicetypefiltervalues;
    
    private List<String> postdevicedetaildevicesubtypefiltervalues;
	
	//Device Validation
	private List<String> postdeviceprovidermanagementvalidate;
	
	//FTPchip detail
    
    private List<String> postftpchipdetail;
	
	private List<String> putftpchipdetail;
    
    private List<String> patchftpchipdetail;

    private List<String> postftpchipdetailuploadcertificate;
     
    private List<String> getftpchipdetailgetpartnercertificate;
	
	private List<String> postftpchipdetailsearch;

	//misplicense controller

    private List<String> postmisps;

    private List<String> putmisps;

    private List<String> getmisps;

    private List<String> getmispsmispidlicensekey;


    //partner-management-controller

    private List<String> putpartnerspartneridapikeypolicies;

    private List<String> patchpartnerspartnerid;

    private List<String> patchpartnerspartneridapikey;

    private List<String> getpartnerspartneridapikeymisplicensekey;

    private List<String> patchpartnersapikey;

    private List<String> getpartners;

    private List<String> getpartnerspartneridapikey;

    private List<String> getpartnersapikey;

    private List<String> getpartnersapikeykey;


    //partnerservice controller
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

    private List<String> putpartneridpolicygrouppolicygroupname;

    private List<String> putemailverify;


	//Registered Device
    
    private List<String> postregistereddevices;

    private List<String> postregistereddevicesderegister;
    
    private List<String> postregistereddevicessearch;
	
	// Secure Biometric Interface
	
	private List<String> postsecurebiometricinterface;
    
    private List<String> putsecurebiometricinterface;

    private List<String> postsecurebiometricinterfacesearch;
     
    private List<String> patchsecurebiometricinterface;
    //Policy management

    private List<String> postpoliciesgroupnew;

    private List<String> putpoliciesgrouppolicygroupid;

    private List<String> postpolicies;

    private List<String> postpoliciespolicyidgrouppublish;

    private List<String> putpoliciespolicyid;

    private List<String> patchpoliciespolicyidgrouppolicygroupid;

    private List<String> getpolicies;

    private List<String> getpoliciespolicyid;

    private List<String> getpoliciesapikey;

    private List<String> getpoliciespolicyidpartnerpartnerid;

    private List<String> getpoliciesgrouppolicygroupid;

    private List<String> getpoliciesgroupall;

    private List<String> postpoliciesgroupsearch;

    private List<String> postpoliciessearch;

    private List<String> getpoliciesconfigkey;

    private List<String> postpoliciesgroupfiltervalues;

    private List<String> postpoliciesfiltervalues;

    private List<String> getactivegroupgroupname;

		
}