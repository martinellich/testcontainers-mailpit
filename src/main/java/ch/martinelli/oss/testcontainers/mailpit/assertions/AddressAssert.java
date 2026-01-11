package ch.martinelli.oss.testcontainers.mailpit.assertions;

import ch.martinelli.oss.testcontainers.mailpit.Address;
import org.assertj.core.api.AbstractAssert;

/**
 * AssertJ assertions for {@link Address}.
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * assertThat(address)
 *     .hasAddress("user@example.com")
 *     .hasName("John Doe")
 *     .isInDomain("example.com");
 * }</pre>
 */
public class AddressAssert extends AbstractAssert<AddressAssert, Address> {

	public AddressAssert(Address actual) {
		super(actual, AddressAssert.class);
	}

	/**
	 * Verifies the email address.
	 * @param expectedAddress the expected email address
	 * @return this assertion object
	 */
	public AddressAssert hasAddress(String expectedAddress) {
		isNotNull();
		if (!expectedAddress.equals(actual.address())) {
			failWithMessage("Expected address to be <%s> but was <%s>", expectedAddress, actual.address());
		}
		return this;
	}

	/**
	 * Verifies the display name.
	 * @param expectedName the expected display name
	 * @return this assertion object
	 */
	public AddressAssert hasName(String expectedName) {
		isNotNull();
		if (!expectedName.equals(actual.name())) {
			failWithMessage("Expected name to be <%s> but was <%s>", expectedName, actual.name());
		}
		return this;
	}

	/**
	 * Verifies the address has a display name.
	 * @return this assertion object
	 */
	public AddressAssert hasDisplayName() {
		isNotNull();
		if (actual.name() == null || actual.name().isEmpty()) {
			failWithMessage("Expected address to have a display name but it was empty");
		}
		return this;
	}

	/**
	 * Verifies the address has no display name.
	 * @return this assertion object
	 */
	public AddressAssert hasNoDisplayName() {
		isNotNull();
		if (actual.name() != null && !actual.name().isEmpty()) {
			failWithMessage("Expected address to have no display name but it was <%s>", actual.name());
		}
		return this;
	}

	/**
	 * Verifies the email address is in the expected domain.
	 * @param domain the expected domain (e.g., "example.com")
	 * @return this assertion object
	 */
	public AddressAssert isInDomain(String domain) {
		isNotNull();
		if (actual.address() == null || !actual.address().endsWith("@" + domain)) {
			failWithMessage("Expected address to be in domain <%s> but was <%s>", domain, actual.address());
		}
		return this;
	}

	/**
	 * Verifies the email address contains the given substring.
	 * @param substring the expected substring
	 * @return this assertion object
	 */
	public AddressAssert addressContains(String substring) {
		isNotNull();
		if (actual.address() == null || !actual.address().contains(substring)) {
			failWithMessage("Expected address to contain <%s> but was <%s>", substring, actual.address());
		}
		return this;
	}

}
