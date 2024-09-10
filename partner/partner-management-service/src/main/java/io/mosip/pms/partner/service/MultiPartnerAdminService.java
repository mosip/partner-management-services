package io.mosip.pms.partner.service;

import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;

public interface MultiPartnerAdminService {

    public ResponseWrapper<Boolean> approveOrRejectDeviceWithSbiMapping(SbiAndDeviceMappingRequestDto requestDto, boolean rejectFlag);
}
