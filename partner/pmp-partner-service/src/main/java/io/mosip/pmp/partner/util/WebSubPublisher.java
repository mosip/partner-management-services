package io.mosip.pmp.partner.util;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.pmp.authdevice.util.dto.Event;
import io.mosip.pmp.authdevice.util.dto.EventModel;
import io.mosip.pmp.authdevice.util.dto.Type;
import io.mosip.pmp.partner.constant.EventType;

public class WebSubPublisher {

	private static final Logger logger = LoggerFactory.getLogger(WebSubPublisher.class);
	
	@Value("${websub.publish.url}")
	private String webSubHubPublishUrl;
	
	@Autowired
	private PublisherClient<String, EventModel, HttpHeaders> pb;
	
	public void notify(EventType eventType,Map<String,Object> data,Type type) {
		sendEventToIDA(createEventModel(eventType,data,type));
	}
	
	private void sendEventToIDA(EventModel model) {
		try {
			logger.info(this.getClass().getSimpleName(), "sendEventToIDA", "Trying registering topic: " + model.getTopic());
			pb.registerTopic(model.getTopic(), webSubHubPublishUrl);
		} catch (Exception e) {
			//Exception will be there if topic already registered. Ignore that
			logger.warn(this.getClass().getSimpleName(), "sendEventToIDA", "Error in registering topic: " + model.getTopic() + " : " + e.getMessage() );
		}
		logger.info(this.getClass().getSimpleName(), "sendEventToIDA", "Publising event to topic: " + model.getTopic());
		pb.publishUpdate(model.getTopic(), model, MediaType.APPLICATION_JSON_VALUE, null, webSubHubPublishUrl);
	}
	
	private EventModel createEventModel(EventType eventType,Map<String,Object> data,Type type) {
		EventModel model = new EventModel();
		model.setPublisher(type.getName());
		String dateTime = DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime());
		model.setPublishedOn(dateTime);
		Event event = new Event();
		event.setTimestamp(dateTime);
		String eventId = UUID.randomUUID().toString();
		event.setId(eventId);
		event.setType(type);
		event.setData(data);
		model.setEvent(event);
		model.setTopic(eventType.toString());
		return model;
	}
}
