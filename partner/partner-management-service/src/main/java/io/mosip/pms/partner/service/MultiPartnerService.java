package io.mosip.pms.partner.service;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.*;

import java.util.List;

public interface MultiPartnerService {

    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAuthPartnersPolicies();

}
