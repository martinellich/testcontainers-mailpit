package ch.martinelli.oss.testcontainers.mailpit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Mailpit.
 * <p>
 * These properties are used when Mailpit is configured via properties rather than via a
 * Testcontainer with {@code @ServiceConnection}.
 *
 * @param host the SMTP host address
 * @param port the SMTP port
 * @param httpUrl the base URL for the Mailpit web interface and REST API
 */
@ConfigurationProperties(prefix = "mailpit")
public record MailpitProperties(String host, int port, String httpUrl) {

	/**
	 * Creates a new MailpitProperties with default values.
	 */
	public MailpitProperties() {
		this("localhost", 1025, "http://localhost:8025");
	}

}
