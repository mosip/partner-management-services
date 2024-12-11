package io.mosip.pms.partner.response.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Class representing All CA Certificate Data Response")
public class CaCertTypeListResponseDto {
    private int pageNumber;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
    private List<CaCertificateSummaryDto> allPartnerCertificates;

}
