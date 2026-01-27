package ch.martinelli.oss.testcontainers.mailpit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

	@Test
	void shouldFormatToStringWithName() {
		Address address = new Address("John Doe", "john@example.com");

		assertThat(address).hasToString("John Doe <john@example.com>");
	}

	@Test
	void shouldFormatToStringWithoutName() {
		Address address = new Address(null, "john@example.com");

		assertThat(address).hasToString("john@example.com");
	}

	@Test
	void shouldFormatToStringWithEmptyName() {
		Address address = new Address("", "john@example.com");

		assertThat(address).hasToString("john@example.com");
	}

	@Test
	void shouldReturnNameAndAddress() {
		Address address = new Address("Jane Doe", "jane@example.com");

		assertThat(address.name()).isEqualTo("Jane Doe");
		assertThat(address.address()).isEqualTo("jane@example.com");
	}

}
