package ch.martinelli.oss.testcontainers.mailpit;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

/**
 * Connection details for a Mailpit service.
 * <p>
 * This interface provides the necessary connection information for configuring a mail
 * sender to use Mailpit as the SMTP server in tests.
 *
 * @see MailpitContainer
 * @see org.springframework.boot.testcontainers.service.connection.ServiceConnection
 */
public interface MailpitConnectionDetails extends ConnectionDetails {

	/**
	 * Returns the SMTP host address.
	 * @return the SMTP host
	 */
	String getHost();

	/**
	 * Returns the SMTP port.
	 * @return the SMTP port
	 */
	int getPort();

	/**
	 * Returns the base URL for the Mailpit web interface and REST API.
	 * @return the HTTP URL (e.g., "http://localhost:32789")
	 */
	String getHttpUrl();

}
