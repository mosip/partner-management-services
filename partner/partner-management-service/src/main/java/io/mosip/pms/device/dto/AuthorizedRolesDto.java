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
		
}