package io.mosip.pms.policy.dto;

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
	private String id;
	
	/**
	 * Auth policy name.
	 */
	private String name;
	
	/**
	 * Auth policy description.
	 */
	private String desc;
	
	/**
	 * Auth policy Status
	 */
	private boolean is_Active;
	
	/**
	 * Created By
	 */
	private String cr_by;	
	
	/**
	 * Created date and time.
	 */
	private LocalDateTime cr_dtimes;
	
	/**
	 * Updated By
	 */
	private String up_by;
	
	/**
	 * Updated date and time
	 */
	private LocalDateTime upd_dtimes;

}
