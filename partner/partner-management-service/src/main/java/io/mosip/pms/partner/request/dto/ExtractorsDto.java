package io.mosip.pms.partner.request.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExtractorsDto {
	
	@Valid
	@NotNull(message = "extractors is missing")
	@NotEmpty(message = "extractors must not be empty")
	private List<ExtractorDto> extractors;

}
