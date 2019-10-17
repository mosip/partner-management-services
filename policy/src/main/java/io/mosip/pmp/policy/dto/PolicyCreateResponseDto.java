package io.mosip.pmp.policy.dto;

import java.time.LocalDateTime;

import lombok.Data;
/**
 * <p> Encapsulates the response details of auth policy</p>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 */

@Data
public class PolicyCreateResponseDto {
	
	/**
	 * policy group id.
	 */
	public String id;
	
	/**
	 * policy group name
	 */
	public String name;
	
	/**
	 * policy group description.
	 */
	public String desc;
	
	/**
	 * policy group status(Active or De-Active)
	 */
	public Boolean is_Active;
	
	/**
	 * Created By
	 */
	public String cr_by;	
	
	/**
	 * Created date and time
	 */
	public LocalDateTime cr_dtimes;
	
	/**
	 * Updated by
	 */
	public String up_by;
	
	/**
	 * Updated date and time
	 */
	public LocalDateTime upd_dtimes;

}
