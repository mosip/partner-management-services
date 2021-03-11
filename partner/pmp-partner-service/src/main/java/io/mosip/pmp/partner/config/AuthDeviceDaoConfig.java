package io.mosip.pmp.partner.config;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This class declares the @Bean methods related to data access using hibernate
 * and will be processed by the Spring container to generate bean definitions
 * and service requests for those beans at runtime
 * 
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "authDeviceEntityManagerFactory",basePackages = {"io.mosip.pmp.authdevice.repository"},
transactionManagerRef = "authDeviceTransactionManager")
public class AuthDeviceDaoConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthDeviceDaoConfig.class);

	/**
	 * Field for interface representing the environment in which the current
	 * application is running.
	 */
	@Autowired
	private Environment environment;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#dataSource()
	 */

	@Value("${hikari.maximumPoolSize:25}")
	private int maximumPoolSize;
	@Value("${hikari.validationTimeout:3000}")
	private int validationTimeout;
	@Value("${hikari.connectionTimeout:60000}")
	private int connectionTimeout;
	@Value("${hikari.idleTimeout:200000}")
	private int idleTimeout;
	@Value("${hikari.minimumIdle:0}")
	private int minimumIdle;

	//@Override
	@Bean(name="authDeviceDataSource")
	public DataSource authDeviceDataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(environment.getProperty(AuthDbPersistenceConstant.JDBC_DRIVER));
		hikariConfig.setJdbcUrl(environment.getProperty(AuthDbPersistenceConstant.JDBC_URL));
		hikariConfig.setUsername(environment.getProperty(AuthDbPersistenceConstant.JDBC_USER));
		hikariConfig.setPassword(environment.getProperty(AuthDbPersistenceConstant.JDBC_PASS));
		if (environment.containsProperty(AuthDbPersistenceConstant.JDBC_SCHEMA)) {
			hikariConfig.setSchema(environment.getProperty(AuthDbPersistenceConstant.JDBC_SCHEMA));
		}
		hikariConfig.setMaximumPoolSize(maximumPoolSize);
		hikariConfig.setValidationTimeout(validationTimeout);
		hikariConfig.setConnectionTimeout(connectionTimeout);
		hikariConfig.setIdleTimeout(idleTimeout);
		hikariConfig.setMinimumIdle(minimumIdle);
		HikariDataSource dataSource = new HikariDataSource(hikariConfig);

		return dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#entityManagerFactory()
	 */
	//@Override
	@PersistenceContext(unitName = "authDeviceEntityManagerFactory")
	@Bean(name="authDeviceEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean authDeviceEntityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(authDeviceDataSource());
		entityManagerFactory.setPackagesToScan(AuthDbPersistenceConstant.MOSIP_PACKAGE);
		entityManagerFactory.setPersistenceUnitName(AuthDbPersistenceConstant.HIBERNATE);
		entityManagerFactory.setJpaPropertyMap(jpaProperties());
		entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter());
		entityManagerFactory.setJpaDialect(jpaDialect());
		return entityManagerFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#jpaVendorAdapter()
	 */
	//@Override
	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);
		vendorAdapter.setShowSql(true);
		return vendorAdapter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#jpaDialect()
	 */
	//@Override
	@Bean
	public JpaDialect jpaDialect() {
		return new HibernateJpaDialect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#transactionManager(javax.
	 * persistence.EntityManagerFactory)
	 */
	//@Override
	@Bean(name="authDeviceTransactionManager")
	public PlatformTransactionManager authDeviceTransactionManager(@Qualifier("authDeviceEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(entityManagerFactory);
		jpaTransactionManager.setDataSource(authDeviceDataSource());
		jpaTransactionManager.setJpaDialect(jpaDialect());
		return jpaTransactionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#jpaProperties()
	 */
	//@Override
	public Map<String, Object> jpaProperties() {
		HashMap<String, Object> jpaProperties = new HashMap<>();
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_HBM2DDL_AUTO,
				AuthDbPersistenceConstant.UPDATE);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_DIALECT,
				AuthDbPersistenceConstant.MY_SQL5_DIALECT);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_SHOW_SQL, AuthDbPersistenceConstant.TRUE);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_FORMAT_SQL,
				AuthDbPersistenceConstant.TRUE);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_CONNECTION_CHAR_SET,
				AuthDbPersistenceConstant.UTF8);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_CACHE_USE_SECOND_LEVEL_CACHE,
				AuthDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_CACHE_USE_QUERY_CACHE,
				AuthDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_CACHE_USE_STRUCTURED_ENTRIES,
				AuthDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_GENERATE_STATISTICS,
				AuthDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_NON_CONTEXTUAL_CREATION,
				AuthDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_CURRENT_SESSION_CONTEXT,
				AuthDbPersistenceConstant.JTA);
		getProperty(jpaProperties, AuthDbPersistenceConstant.HIBERNATE_EJB_INTERCEPTOR,
				AuthDbPersistenceConstant.EMPTY_INTERCEPTOR);
		return jpaProperties;
	}

	/**
	 * Function to associate the specified value with the specified key in the map.
	 * If the map previously contained a mapping for the key, the old value is
	 * replaced.
	 * 
	 * @param jpaProperties The map of jpa properties
	 * @param property      The property whose value is to be set
	 * @param defaultValue  The default value to set
	 * @return The map of jpa properties with properties set
	 */
	private HashMap<String, Object> getProperty(HashMap<String, Object> jpaProperties, String property,
			String defaultValue) {
		/**
		 * if property found in properties file then add that interceptor to the jpa
		 * properties.
		 */
		if (property.equals(AuthDbPersistenceConstant.HIBERNATE_EJB_INTERCEPTOR)) {
			try {
				if (environment.containsProperty(property)) {
					jpaProperties.put(property,
							// encryptionInterceptor());
							BeanUtils.instantiateClass(Class.forName(environment.getProperty(property))));
				}
				/**
				 * We can add a default interceptor whenever we require here.
				 */
			} catch (BeanInstantiationException | ClassNotFoundException e) {
				LOGGER.error("Error while configuring Interceptor.");
			}
		} else {
			jpaProperties.put(property,
					environment.containsProperty(property) ? environment.getProperty(property) : defaultValue);
		}
		return jpaProperties;
	}
}
