/*
 * Copyright 2011-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.springframework.boot.autoconfigure;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.EventListener;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jConnectionDetails;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

/**
 * This provides the beans necessary to run SDN+OGM much like Spring Boot prior to 2.4 did.
 * It can be used as a blueprint for your own integration. Take note of the inline comments.
 *
 * @author Michael J. Simons
 */
@AutoConfiguration
@ConditionalOnClass(SessionFactory.class)
@ConditionalOnBean(Driver.class)
@AutoConfigureAfter( {Neo4jAutoConfiguration.class})
@AutoConfigureBefore( {Neo4jDataAutoConfiguration.class})
@EnableConfigurationProperties( {Neo4jOGMProperties.class})
public class Neo4jOGMAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(PlatformTransactionManager.class)
	public Neo4jTransactionManager transactionManager(SessionFactory sessionFactory,
	                                                  ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
		Neo4jTransactionManager transactionManager = new Neo4jTransactionManager(sessionFactory);
		transactionManagerCustomizers.ifAvailable((customizers) -> customizers.customize(transactionManager));
		return transactionManager;
	}

	@Bean
	@ConditionalOnMissingBean
	public org.neo4j.ogm.config.Configuration configuration(
			Neo4jConnectionDetails springBootNeo4jConnectionDetails,
			Neo4jOGMProperties ogmProperties
	) {
		var builder = new Configuration.Builder()
				// Actually not needed for the driver to work, but required for the config not to stumble upon null
				.uri(springBootNeo4jConnectionDetails.getUri().toString());

		if (ogmProperties.isUseNativeTypes()) {
			builder.useNativeTypes();
		}
		if (ogmProperties.isUseStrictQuerying()) {
			builder.strictQuerying();
		}
		builder.withBasePackages(ogmProperties.getBasePackages());
		return builder.build();
	}

	@Bean
	@ConditionalOnMissingBean
	public BoltDriver ogmDriver(org.neo4j.ogm.config.Configuration ogmConfiguration, Driver nativeDriver) {

		BoltDriver boltDriver = new BoltDriver(nativeDriver) {
			@Override
			public synchronized void close() {
				// We must prevent the bolt driver from closing the driver bean
			}
		};
		boltDriver.configure(ogmConfiguration);
		return boltDriver;
	}

	@Bean
	@ConditionalOnMissingBean(SessionFactory.class)
	SessionFactory sessionFactory(org.neo4j.ogm.config.Configuration configuration, BoltDriver ogmDriver,
	                              BeanFactory beanFactory,
	                              ObjectProvider<EventListener> eventListeners) {

		var packages = configuration.getBasePackages();
		if (packages == null || packages.length == 0) {
			packages = getPackagesToScan(beanFactory);
		}

		SessionFactory sessionFactory = new SessionFactory(ogmDriver, packages);
		if (configuration.getAutoIndex() != AutoIndexMode.NONE) {
			sessionFactory.runAutoIndexManager(configuration);
		}
		eventListeners.orderedStream().forEach(sessionFactory::register);
		return sessionFactory;
	}

	private String[] getPackagesToScan(BeanFactory beanFactory) {
		List<String> packages = EntityScanPackages.get(beanFactory).getPackageNames();
		if (packages.isEmpty() && AutoConfigurationPackages.has(beanFactory)) {
			packages = AutoConfigurationPackages.get(beanFactory);
		}
		return StringUtils.toStringArray(packages);
	}
}
