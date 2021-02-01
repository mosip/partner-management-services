package io.mosip.pmp.misp.dto;

import java.util.List;

import io.mosip.pms.common.entity.MISPEntity;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0 
 * @since 2019-Oct
 * 
 * Encapsulates multiple misp details.
 */
@Data
public class MISPGroupResponseDto {

	/**
	 * Provide multiple misp details.{@link MISPEntity}
	 */
	private List<MISPEntity> mispList;
}
