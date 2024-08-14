package io.mosip.pms.partner.service;

import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;

public interface MultiPartnerAdminService {

    public Boolean approveDeviceWithSbiMapping(SbiAndDeviceMappingRequestDto requestDto);
}
