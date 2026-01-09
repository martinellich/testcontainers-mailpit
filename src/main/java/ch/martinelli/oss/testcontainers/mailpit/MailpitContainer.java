package ch.martinelli.oss.testcontainers.mailpit;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers implementation for Mailpit.
 * <p>
 * Mailpit is an email and SMTP testing tool with API for developers. It runs a simple
 * SMTP server which catches any message sent to it and displays it in a web interface.
 * <p>
 * Exposed ports:
 * <ul>
 * <li>1025 - SMTP port for sending emails</li>
 * <li>8025 - HTTP port for the web interface and REST API</li>
 * </ul>
 *
 * @see <a href="https://hub.docker.com/r/axllent/mailpit">Mailpit Docker Image</a>
 * @see <a href="https://mailpit.axllent.org/">Mailpit Documentation</a>
 */
public class MailpitContainer extends GenericContainer<MailpitContainer> {

	private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("axllent/mailpit");

	private static final String DEFAULT_TAG = "latest";

	public static final int SMTP_PORT = 1025;

	public static final int HTTP_PORT = 8025;

	public MailpitContainer() {
		this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
	}

	public MailpitContainer(String dockerImageName) {
		this(DockerImageName.parse(dockerImageName));
	}

	public MailpitContainer(DockerImageName dockerImageName) {
		super(dockerImageName);
		dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

		withExposedPorts(SMTP_PORT, HTTP_PORT);
		waitingFor(Wait.forHttp("/").forPort(HTTP_PORT));
	}

	/**
	 * Returns the mapped SMTP port for sending emails.
	 * @return the mapped SMTP port
	 */
	public int getSmtpPort() {
		return getMappedPort(SMTP_PORT);
	}

	/**
	 * Returns the mapped HTTP port for the web interface and REST API.
	 * @return the mapped HTTP port
	 */
	public int getHttpPort() {
		return getMappedPort(HTTP_PORT);
	}

	/**
	 * Returns the SMTP host address.
	 * @return the SMTP host
	 */
	public String getSmtpHost() {
		return getHost();
	}

	/**
	 * Returns the base URL for the Mailpit web interface and REST API.
	 * @return the base URL (e.g., "http://localhost:32789")
	 */
	public String getHttpUrl() {
		return String.format("http://%s:%d", getHost(), getHttpPort());
	}

	/**
	 * Returns a {@link MailpitClient} for interacting with the Mailpit REST API.
	 * @return a new MailpitClient instance
	 */
	public MailpitClient getClient() {
		return new MailpitClient(getHttpUrl());
	}

}
