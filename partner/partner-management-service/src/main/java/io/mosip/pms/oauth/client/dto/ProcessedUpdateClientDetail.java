package io.mosip.pms.oauth.client.dto;

import io.mosip.pms.common.entity.ClientDetail;
import io.mosip.pms.common.entity.Partner;
import lombok.Data;

@Data
public class ProcessedUpdateClientDetail {
    private ClientDetail clientDetail;
    private Partner partner;
}
