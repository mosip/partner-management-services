package io.mosip.pmp.policy.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 *  <p> Encapsulates the response details of auth policy.</p>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 */
@Data
public class AuthPolicyCreateResponseDto {

	/**
	 * Auth policy id
	 * 
	 */
	public String id;
	
	/**
	 * Auth policy name.
	 */
	public String name;
	
	/**
	 * Auth policy description.
	 */
	public String desc;
	
	/**
	 * Auth policy Status
	 */
	public boolean is_Active;
	
	/**
	 * Created By
	 */
	public String cr_by;	
	
	/**
	 * Created date and time.
	 */
	public LocalDateTime cr_dtimes;
	
	/**
	 * Updated By
	 */
	public String up_by;
	
	/**
	 * Updated date and time
	 */
	public LocalDateTime upd_dtimes;

}
