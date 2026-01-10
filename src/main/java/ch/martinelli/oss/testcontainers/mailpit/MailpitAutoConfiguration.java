package ch.martinelli.oss.testcontainers.mailpit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Mailpit.
 * <p>
 * This configuration provides default beans for {@link MailpitConnectionDetails} and
 * {@link MailpitClient} when not using a Testcontainer with {@code @ServiceConnection}.
 * <p>
 * When using {@code @ServiceConnection} with a {@link MailpitContainer}, the
 * {@link MailpitContainerConnectionDetailsFactory} provides the connection details
 * directly from the container, and these defaults are not used.
 *
 * @see MailpitConnectionDetails
 * @see MailpitContainerConnectionDetailsFactory
 */
@AutoConfiguration
@EnableConfigurationProperties(MailpitProperties.class)
public class MailpitAutoConfiguration {

	/**
	 * Creates a {@link MailpitConnectionDetails} bean backed by {@link MailpitProperties}
	 * when no other {@link MailpitConnectionDetails} bean is present.
	 * <p>
	 * This bean is typically overridden when using {@code @ServiceConnection} with a
	 * {@link MailpitContainer}.
	 * @param properties the Mailpit configuration properties
	 * @return the connection details
	 */
	@Bean
	@ConditionalOnMissingBean(MailpitConnectionDetails.class)
	MailpitConnectionDetails mailpitConnectionDetails(MailpitProperties properties) {
		return new PropertiesMailpitConnectionDetails(properties);
	}

	/**
	 * Creates a {@link MailpitClient} bean using the provided
	 * {@link MailpitConnectionDetails}.
	 * <p>
	 * This client can be autowired in tests to interact with the Mailpit REST API.
	 * @param connectionDetails the Mailpit connection details
	 * @return the Mailpit client
	 */
	@Bean
	@ConditionalOnMissingBean(MailpitClient.class)
	MailpitClient mailpitClient(MailpitConnectionDetails connectionDetails) {
		return new MailpitClient(connectionDetails.getHttpUrl());
	}

}
