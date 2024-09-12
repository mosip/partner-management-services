package io.mosip.pms.partner.service;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;

public interface MultiPartnerAdminService {

    public ResponseWrapperV2<Boolean> approveOrRejectDeviceWithSbiMapping(SbiAndDeviceMappingRequestDto requestDto, boolean rejectFlag);
}
