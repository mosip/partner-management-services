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