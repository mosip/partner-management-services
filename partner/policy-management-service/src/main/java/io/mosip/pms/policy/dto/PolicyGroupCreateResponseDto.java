package io.mosip.pms.policy.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PolicyGroupCreateResponseDto {

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
	 * policy group status(Active or De-Active)
	 */
	private Boolean is_Active;
	
	/**
	 * Created By
	 */
	private String cr_by;	
	
	/**
	 * Created date and time
	 */
	private LocalDateTime cr_dtimes;
	
	/**
	 * Updated by
	 */
	private String up_by;
	
	/**
	 * Updated date and time
	 */
	private LocalDateTime upd_dtimes;

}
