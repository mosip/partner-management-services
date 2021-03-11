package io.mosip.pmp.policy.dto;

import lombok.Data;

@SuppressWarnings("hiding")
@Data
public class KeyValuePair<String, V> {
	
	public KeyValuePair(String key,V value) {
		this.key = key;
		this.value = value;
	}
	
	private String key;
	
	private V value;

}
