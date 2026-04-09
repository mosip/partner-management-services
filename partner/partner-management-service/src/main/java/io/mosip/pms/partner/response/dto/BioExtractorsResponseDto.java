package io.mosip.pms.partner.response.dto;

import java.util.List;

import lombok.Data;

import io.mosip.pms.partner.request.dto.BioExtractorsDto;

@Data
public class BioExtractorsResponseDto {
	private List<BioExtractorsDto> extractors;
}

