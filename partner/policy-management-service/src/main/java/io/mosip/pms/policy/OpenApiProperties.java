package io.mosip.pms.policy;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author Nagarjuna Kuchi
 *
 */
@Configuration
@ConfigurationProperties(prefix = "openapi")
@Data
public class OpenApiProperties {
	private InfoProperty info;
	private PolicyManagementService policyManagementService;
}

/**
 * @author Nagarjuna Kuchi
 *
 */
@Data
class InfoProperty {
	private String title;
	private String description;
	private String version;
	private LicenseProperty license;
}

/**
 * @author Nagarjuna Kuchi
 *
 */
@Data
class LicenseProperty {
	private String name;
	private String url;
}

/**
 * @author Nagarjuna Kuchi
 *
 */
@Data
class PolicyManagementService {
	private List<Server> servers;
}

/**
 * @author Nagarjuna Kuchi
 *
 */
@Data
class Server {
	private String description;
	private String url;
}
