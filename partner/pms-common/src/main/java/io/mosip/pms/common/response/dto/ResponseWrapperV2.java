package io.mosip.pms.common.response.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.pms.common.request.dto.ErrorResponse;
import lombok.Data;

@Data
public class ResponseWrapperV2<T> {
    private String id;
    private String version;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime responseTime = LocalDateTime.now(ZoneId.of("UTC"));
    private Object metadata;
    @NotNull
    @Valid
    private T response;

    private List<ErrorResponse> errors = new ArrayList<>();

}