package io.mosip.pms.oidc.client.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationFactor {

    private String type;
    private int count;
    private List<String> subTypes;
}
