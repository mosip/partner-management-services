package io.mosip.pms.ida.dto;

import java.util.Map;

import lombok.Data;

@Data
public class Event {
	//uuid event id to be create and put in loggers
	private String id;
	
	private Type type;
	
	private String timestamp;
	
	private Map<String,Object> data;
	
}
