package io.mosip.pms.partner.response.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({ "id", "version", "responseTime", "metadata", "partnerPolicyRequestId", "statusCode", "response", "errors" })
public class BioExtractorsResponseWrapperV2 extends ResponseWrapperV2<BioExtractorsResponseDto> {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String partnerPolicyRequestId;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String statusCode;
}

