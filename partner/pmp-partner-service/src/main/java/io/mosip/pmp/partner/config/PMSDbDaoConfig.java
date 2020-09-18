package io.mosip.pmp.partner.config;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

import io.mosip.kernel.core.dataaccess.spi.config.BaseDaoConfig;

/**
 * This class declares the @Bean methods related to data access using hibernate
 * and will be processed by the Spring container to generate bean definitions
 * and service requests for those beans at runtime
 * 
 * @author Nagarjuna
 * 
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "pmsEntityManagerFactory",basePackages = {"io.mosip.pmp.partner.repository"},
transactionManagerRef = "pmsPlatformTransactionManager")
public class PMSDbDaoConfig implements BaseDaoConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(PMSDbDaoConfig.class);

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

	@Primary
	@Override
	@Bean(name="pmsDataSource")
	public DataSource dataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(environment.getProperty(PMSDbPersistenceConstant.JDBC_DRIVER));
		hikariConfig.setJdbcUrl(environment.getProperty(PMSDbPersistenceConstant.JDBC_URL));
		hikariConfig.setUsername(environment.getProperty(PMSDbPersistenceConstant.JDBC_USER));
		hikariConfig.setPassword(environment.getProperty(PMSDbPersistenceConstant.JDBC_PASS));
		if (environment.containsProperty(PMSDbPersistenceConstant.JDBC_SCHEMA)) {
			hikariConfig.setSchema(environment.getProperty(PMSDbPersistenceConstant.JDBC_SCHEMA));
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
	@Primary
	@Override
	@Bean(name="pmsEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(dataSource());
		entityManagerFactory.setPackagesToScan(PMSDbPersistenceConstant.MOSIP_PACKAGE);
		entityManagerFactory.setPersistenceUnitName(PMSDbPersistenceConstant.HIBERNATE);
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
	@Primary
	@Override
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
	@Primary
	@Override
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
	@Primary
	@Override
	@Bean(name="pmsPlatformTransactionManager")
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(entityManagerFactory);
		jpaTransactionManager.setDataSource(dataSource());
		jpaTransactionManager.setJpaDialect(jpaDialect());
		return jpaTransactionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#jpaProperties()
	 */
	@Override
	public Map<String, Object> jpaProperties() {
		HashMap<String, Object> jpaProperties = new HashMap<>();
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_HBM2DDL_AUTO,
				PMSDbPersistenceConstant.UPDATE);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_DIALECT,
				PMSDbPersistenceConstant.MY_SQL5_DIALECT);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_SHOW_SQL, PMSDbPersistenceConstant.TRUE);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_FORMAT_SQL,
				PMSDbPersistenceConstant.TRUE);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_CONNECTION_CHAR_SET,
				PMSDbPersistenceConstant.UTF8);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_CACHE_USE_SECOND_LEVEL_CACHE,
				PMSDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_CACHE_USE_QUERY_CACHE,
				PMSDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_CACHE_USE_STRUCTURED_ENTRIES,
				PMSDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_GENERATE_STATISTICS,
				PMSDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_NON_CONTEXTUAL_CREATION,
				PMSDbPersistenceConstant.FALSE);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_CURRENT_SESSION_CONTEXT,
				PMSDbPersistenceConstant.JTA);
		getProperty(jpaProperties, PMSDbPersistenceConstant.HIBERNATE_EJB_INTERCEPTOR,
				PMSDbPersistenceConstant.EMPTY_INTERCEPTOR);
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
		if (property.equals(PMSDbPersistenceConstant.HIBERNATE_EJB_INTERCEPTOR)) {
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
