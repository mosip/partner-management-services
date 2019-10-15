package io.mosip.pmp.misp.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to hold the misp details along with misp licenses.
 *
 */
@Data
public class MISPWithLicenseDto implements Serializable {

	public static final long serialVersionUID = 6670743917275798523L;
	
	public String id;
	
	public String organizationName;
	
	public String contactNumber;
	
	public String emailID;
	
	public String address;
	
	public String status;

	public List<MISPLiceneseDto> licenses;
}
