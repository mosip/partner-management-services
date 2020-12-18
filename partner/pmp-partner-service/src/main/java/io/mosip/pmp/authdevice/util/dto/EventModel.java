package io.mosip.pmp.authdevice.util.dto;

import lombok.Data;

@Data
public class EventModel {

	private String publisher;
	private String topic;
	private String publishedOn;
	private Event event;
}
