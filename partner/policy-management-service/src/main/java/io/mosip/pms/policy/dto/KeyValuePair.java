package io.mosip.pms.policy.dto;

import lombok.Data;

@Data
public class KeyValuePair<String, V> {
	
	public KeyValuePair(String key,V value) {
		this.key = key;
		this.value = value;
	}
	
	private String key;
	
	private V value;

}
