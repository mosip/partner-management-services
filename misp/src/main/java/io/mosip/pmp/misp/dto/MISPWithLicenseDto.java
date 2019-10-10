package io.mosip.pmp.misp.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class MISPWithLicenseDto implements Serializable {

	private static final long serialVersionUID = 6670743917275798523L;
	
	private String id;
	
	private String organizationName;
	
	private String contactNumber;
	
	private String emailID;
	
	private String address;
	
	private String status;

	private List<MISPLiceneseDto> licenses;
}
