package io.mosip.pms.common.response.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.mosip.pms.common.request.dto.ErrorResponse;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "version", "responseTime", "metadata", "partnerPolicyRequestId", "statusCode", "response", "errors" })
public class ResponseWrapperV2<T> {
    private String id;
    private String version;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime responseTime = LocalDateTime.now(ZoneId.of("UTC"));
    private Object metadata;
    /**
     * Optional; set for APIs that expose a partner policy request id at wrapper level (e.g. bio-extractors GET).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String partnerPolicyRequestId;
    /**
     * Optional; set for APIs that expose a status at wrapper level (e.g. partner policy request status).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String statusCode;
    @NotNull
    @Valid
    private T response;

    private List<ErrorResponse> errors = new ArrayList<>();

}