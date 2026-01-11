package ch.martinelli.oss.testcontainers.mailpit.assertions;

import ch.martinelli.oss.testcontainers.mailpit.Address;
import ch.martinelli.oss.testcontainers.mailpit.MailpitContainer;
import ch.martinelli.oss.testcontainers.mailpit.Message;

import java.util.List;

/**
 * Entry point for AssertJ-style assertions for Mailpit.
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * import static ch.martinelli.oss.testcontainers.mailpit.assertions.MailpitAssertions.assertThat;
 *
 * assertThat(mailpitContainer)
 *     .hasMessages()
 *     .hasMessageCount(1);
 *
 * assertThat(mailpitContainer)
 *     .awaitMessage()
 *     .withSubject("Welcome")
 *     .isPresent();
 * }</pre>
 */
public final class MailpitAssertions {

	private MailpitAssertions() {
	}

	/**
	 * Creates assertions for a MailpitContainer.
	 * @param actual the container to assert on
	 * @return a new MailpitContainerAssert instance
	 */
	public static MailpitContainerAssert assertThat(MailpitContainer actual) {
		return new MailpitContainerAssert(actual);
	}

	/**
	 * Creates assertions for a Message.
	 * @param actual the message to assert on
	 * @return a new MessageAssert instance
	 */
	public static MessageAssert assertThat(Message actual) {
		return new MessageAssert(actual);
	}

	/**
	 * Creates assertions for an Address.
	 * @param actual the address to assert on
	 * @return a new AddressAssert instance
	 */
	public static AddressAssert assertThat(Address actual) {
		return new AddressAssert(actual);
	}

	/**
	 * Creates assertions for a list of Messages.
	 * @param actual the messages to assert on
	 * @return a new MessagesAssert instance
	 */
	public static MessagesAssert assertThat(List<Message> actual) {
		return new MessagesAssert(actual);
	}

}
