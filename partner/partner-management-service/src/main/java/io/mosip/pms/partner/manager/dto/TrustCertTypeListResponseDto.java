package io.mosip.pms.partner.manager.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Class representing All CA Certificate Data Response")
public class TrustCertTypeListResponseDto {
    private int pageNumber;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
    private List<TrustCertificateSummaryDto> allPartnerCertificates;

}
