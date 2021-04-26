package io.mosip.pms.common.response.dto;

import java.util.List;

import lombok.Data;

@Data
public class TemplatesResponseDto {
	List<TemplateResponseDto> templates;
}