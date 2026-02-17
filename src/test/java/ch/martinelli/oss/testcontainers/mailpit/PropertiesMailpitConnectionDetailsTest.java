package ch.martinelli.oss.testcontainers.mailpit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesMailpitConnectionDetailsTest {

	@Test
	void shouldReturnHostFromProperties() {
		MailpitProperties properties = new MailpitProperties("localhost", 1025, "http://localhost:8025");
		PropertiesMailpitConnectionDetails details = new PropertiesMailpitConnectionDetails(properties);

		assertThat(details.getHost()).isEqualTo("localhost");
	}

	@Test
	void shouldReturnPortFromProperties() {
		MailpitProperties properties = new MailpitProperties("localhost", 2525, "http://localhost:8025");
		PropertiesMailpitConnectionDetails details = new PropertiesMailpitConnectionDetails(properties);

		assertThat(details.getPort()).isEqualTo(2525);
	}

	@Test
	void shouldReturnHttpUrlFromProperties() {
		MailpitProperties properties = new MailpitProperties("localhost", 1025, "http://example.com:8080");
		PropertiesMailpitConnectionDetails details = new PropertiesMailpitConnectionDetails(properties);

		assertThat(details.getHttpUrl()).isEqualTo("http://example.com:8080");
	}

}
