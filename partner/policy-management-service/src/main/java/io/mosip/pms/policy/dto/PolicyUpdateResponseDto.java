package io.mosip.pms.policy.dto;

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
	private String id;
	
	/**
	 * policy group name
	 */
	private String name;
	
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
	private LocalDateTime cr_dtimes;
	
	/**
	 * updated by
	 */
	private String up_by;
	
	/**
	 * updated date and times.
	 */
	private LocalDateTime upd_dtimes;

}
