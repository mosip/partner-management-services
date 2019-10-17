package io.mosip.pmp.policy.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p> Encapsulates the response details of policy update.</p>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 */
@Data
public class PolicyUpdateResponseDto {

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
	private String desc;
	
	/**
	 * policy group Status
	 */
	private boolean is_Active;
	
	/**
	 * created By
	 */
	private String cr_by;	
	
	/**
	 * created date and times.
	 */
	public LocalDateTime cr_dtimes;
	
	/**
	 * updated by
	 */
	public String up_by;
	
	/**
	 * updated date and times.
	 */
	public LocalDateTime upd_dtimes;

}
