package io.mosip.pms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.util.PMSLogger;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
	private static final Logger logger = PMSLogger.getLogger(SwaggerConfig.class);

	@Autowired
	private OpenApiProperties openApiProperties;

	@Bean
	public OpenAPI openApi() {
		String msg = "Swagger open api, ";
		OpenAPI api = new OpenAPI().components(new Components());
		if (null != openApiProperties.getInfo()) {
			api.info(new Info().title(openApiProperties.getInfo().getTitle())
					.version(openApiProperties.getInfo().getVersion())
					.description(openApiProperties.getInfo().getDescription()));
			if (null != openApiProperties.getInfo().getLicense()) {
				api.getInfo().license(new License().name(openApiProperties.getInfo().getLicense().getName())
						.url(openApiProperties.getInfo().getLicense().getUrl()));
				logger.info(msg + "info license property is added");
			} else {
				logger.error(msg + "info license property is empty");
			}
			logger.info(msg + "info property is added");
		} else {
			logger.error(msg + "info property is empty");
		}

		if (null != openApiProperties.getPartnerManagementService().getServers()) {
			openApiProperties.getPartnerManagementService().getServers().forEach(server -> {
				api.addServersItem(new Server().description(server.getDescription()).url(server.getUrl()));
			});
			logger.info(msg + "server property is added");
		} else {
			logger.error(msg + "server property is empty");
		}
		return api;
	}
}
