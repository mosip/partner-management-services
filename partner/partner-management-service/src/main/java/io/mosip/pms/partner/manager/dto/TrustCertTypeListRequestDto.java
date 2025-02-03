package io.mosip.pms.partner.manager.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to get CA/Sub-CA certificates.")
public class TrustCertTypeListRequestDto {

    /**
     * Certificate Type
     */
    @ApiModelProperty(notes = "Partner Certificate Type", required = false)
    String caCertificateType;

    /**
     * Domain Name
     */
    @ApiModelProperty(notes = "Domain Name", required = false)
    String partnerDomain;

    @ApiModelProperty(notes = "Flag to force exclude the mosip CA Certificates", example = "false", required = false)
    private Boolean excludeMosipCA;

    /**
     * CA Certificate Id
     */
    @ApiModelProperty(notes = "CA Certificate Id", required = false)
    private String certId;

    /**
     * Ca Certificate Issued To
     */
    @ApiModelProperty(notes = "Issued To", required = false)
    private String issuedTo;

    /**
     * Ca Certificate Issued By
     */
    @ApiModelProperty(notes = "Issued By", required = false)
    private String issuedBy;

    /**
     * Ca Certificate Valid From
     */
    @ApiModelProperty(notes = "Valid From", required = false)
    private LocalDateTime validFromDate;

    /**
     * Ca Certificate Valid Till
     */
    @ApiModelProperty(notes = "Valid Till", required = false)
    private LocalDateTime validTillDate;

    /**
     * Ca Certificate uploaded time
     */
    @ApiModelProperty(notes = "Upload Time", required = false)
    private LocalDateTime uploadTime;

    /**
     * Sort By Field Name
     */
    @ApiModelProperty(notes = "Sort By Field", required = false)
    private String sortByFieldName;

    /**
     * Sort Direction: ASC, DESC
     */
    @ApiModelProperty(notes = "Sort Direction", required = false)
    String sortOrder;

    /**
     * Page Number
     */
    @ApiModelProperty(notes = "Page Number", required = false)
    @NotNull
    int pageNumber;

    /**
     * Number of Certificate
     */
    @ApiModelProperty(notes = "Number of Certificates", required = false)
    @NotNull
    int pageSize;
}
