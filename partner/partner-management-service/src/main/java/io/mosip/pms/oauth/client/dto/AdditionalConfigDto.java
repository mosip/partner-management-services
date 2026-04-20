package io.mosip.pms.oauth.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;



import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalConfigDto {

    @JsonProperty("userinfo_response_type")
    private String userinfoResponseType;

    @JsonProperty("purpose")
    private Map<String, Object> purpose;

    @JsonProperty("signup_banner_required")
    private Boolean signupBannerRequired;

    @JsonProperty("forgot_pwd_link_required")
    private Boolean forgotPwdLinkRequired;

    @JsonProperty("consent_expire_in_mins")
    private Integer consentExpireInMins;
}
