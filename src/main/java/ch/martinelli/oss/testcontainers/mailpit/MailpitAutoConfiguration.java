package ch.martinelli.oss.testcontainers.mailpit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Auto-configuration for Mailpit.
 * <p>
 * This configuration provides default beans for {@link MailpitConnectionDetails} and
 * {@link MailpitClient} when not using a Testcontainer with {@code @ServiceConnection}.
 * <p>
 * When using {@code @ServiceConnection} with a {@link MailpitContainer}, the
 * {@link MailpitContainerConnectionDetailsFactory} provides the connection details
 * directly from the container, and these defaults are not used.
 * <p>
 * This configuration also provides a {@link JavaMailSender} bean configured from
 * {@link MailpitConnectionDetails} when present.
 *
 * @see MailpitConnectionDetails
 * @see MailpitContainerConnectionDetailsFactory
 */
@AutoConfiguration(beforeName = "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration")
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

	/**
	 * Creates a {@link JavaMailSender} bean configured from
	 * {@link MailpitConnectionDetails}.
	 * <p>
	 * This bean is created when {@link MailpitConnectionDetails} is available (either
	 * from {@code @ServiceConnection} or from properties) and no other
	 * {@link JavaMailSender} is present.
	 * @param connectionDetails the Mailpit connection details
	 * @return the configured JavaMailSender
	 */
	@Bean
	@ConditionalOnClass(JavaMailSender.class)
	@ConditionalOnBean(MailpitConnectionDetails.class)
	@ConditionalOnMissingBean(JavaMailSender.class)
	JavaMailSender javaMailSender(MailpitConnectionDetails connectionDetails) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(connectionDetails.getHost());
		mailSender.setPort(connectionDetails.getPort());
		var props = new Properties();
		props.put("mail.smtp.localhost", "localhost");
		props.put("mail.from", "noreply@localhost");
		mailSender.setJavaMailProperties(props);
		return mailSender;
	}

}
