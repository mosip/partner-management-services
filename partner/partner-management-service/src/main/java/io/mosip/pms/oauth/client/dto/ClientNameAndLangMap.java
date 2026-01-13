package io.mosip.pms.oauth.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ClientNameAndLangMap {

    private String name;

    private Map<String, String > langMap;
}
