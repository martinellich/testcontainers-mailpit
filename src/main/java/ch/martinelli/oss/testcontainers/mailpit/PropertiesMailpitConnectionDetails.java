package ch.martinelli.oss.testcontainers.mailpit;

/**
 * {@link MailpitConnectionDetails} implementation backed by {@link MailpitProperties}.
 * <p>
 * This class provides connection details when Mailpit is configured via properties rather
 * than via a Testcontainer.
 */
class PropertiesMailpitConnectionDetails implements MailpitConnectionDetails {

	private final MailpitProperties properties;

	PropertiesMailpitConnectionDetails(MailpitProperties properties) {
		this.properties = properties;
	}

	@Override
	public String getHost() {
		return this.properties.host();
	}

	@Override
	public int getPort() {
		return this.properties.port();
	}

	@Override
	public String getHttpUrl() {
		return this.properties.httpUrl();
	}

}
