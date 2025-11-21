package io.mosip.pms.oauth.client.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientDetailV2 extends ClientDetail {

    private String policyGroupName;
    private String policyGroupDescription;
    private String policyDescription;
    private Map<String, String> clientNameLangMap;
    private AdditionalConfigDto additionalConfig;

}
