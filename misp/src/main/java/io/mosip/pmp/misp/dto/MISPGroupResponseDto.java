package io.mosip.pmp.misp.dto;

import java.util.List;

import io.mosip.pmp.misp.entity.MISPEntity;
import lombok.Data;

@Data
public class MISPGroupResponseDto {

	private List<MISPEntity> mispList;
}
