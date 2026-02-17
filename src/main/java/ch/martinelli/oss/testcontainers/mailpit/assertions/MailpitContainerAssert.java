package ch.martinelli.oss.testcontainers.mailpit.assertions;

import ch.martinelli.oss.testcontainers.mailpit.MailpitClient;
import ch.martinelli.oss.testcontainers.mailpit.MailpitContainer;
import ch.martinelli.oss.testcontainers.mailpit.Message;
import org.assertj.core.api.AbstractAssert;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.List;

/**
 * AssertJ assertions for {@link MailpitContainer}.
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * assertThat(mailpitContainer)
 *     .isRunning()
 *     .hasMessages()
 *     .hasMessageCount(1);
 *
 * assertThat(mailpitContainer)
 *     .withTimeout(Duration.ofSeconds(30))
 *     .awaitMessage()
 *     .withSubject("Welcome")
 *     .isPresent();
 * }</pre>
 */
@SuppressWarnings("java:S2160")
public class MailpitContainerAssert extends AbstractAssert<MailpitContainerAssert, MailpitContainer> {

	private Duration timeout = Duration.ofSeconds(10);

	private Duration pollInterval = Duration.ofMillis(500);

	public MailpitContainerAssert(MailpitContainer actual) {
		super(actual, MailpitContainerAssert.class);
	}

	/**
	 * Verifies that the container is running.
	 * @return this assertion object
	 */
	public MailpitContainerAssert isRunning() {
		isNotNull();
		if (!actual.isRunning()) {
			failWithMessage("Expected MailpitContainer to be running but it was not");
		}
		return this;
	}

	/**
	 * Verifies that the mailbox contains at least one message.
	 * @return this assertion object
	 */
	public MailpitContainerAssert hasMessages() {
		isNotNull();
		MailpitClient client = actual.getClient();
		if (client.getMessageCount() == 0) {
			failWithMessage("Expected mailbox to contain messages but it was empty");
		}
		return this;
	}

	/**
	 * Verifies that the mailbox is empty.
	 * @return this assertion object
	 */
	public MailpitContainerAssert hasNoMessages() {
		isNotNull();
		MailpitClient client = actual.getClient();
		int count = client.getMessageCount();
		if (count > 0) {
			failWithMessage("Expected mailbox to be empty but found <%d> message(s)", count);
		}
		return this;
	}

	/**
	 * Verifies that the mailbox contains exactly the expected number of messages.
	 * @param expectedCount the expected message count
	 * @return this assertion object
	 */
	public MailpitContainerAssert hasMessageCount(int expectedCount) {
		isNotNull();
		MailpitClient client = actual.getClient();
		int actualCount = client.getMessageCount();
		if (actualCount != expectedCount) {
			failWithMessage("Expected mailbox to contain <%d> message(s) but found <%d>", expectedCount, actualCount);
		}
		return this;
	}

	/**
	 * Configures the timeout for await operations.
	 * @param timeout the maximum time to wait
	 * @return this assertion object
	 */
	public MailpitContainerAssert withTimeout(Duration timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Configures the poll interval for await operations.
	 * @param pollInterval the interval between polls
	 * @return this assertion object
	 */
	public MailpitContainerAssert withPollInterval(Duration pollInterval) {
		this.pollInterval = pollInterval;
		return this;
	}

	/**
	 * Creates a message awaiter to wait for and assert on messages.
	 * @return a new MessageAwaiter for fluent assertions
	 */
	public MessageAwaiter awaitMessage() {
		isNotNull();
		return new MessageAwaiter(actual.getClient(), timeout, pollInterval);
	}

	/**
	 * Waits until the mailbox contains at least one message.
	 * @return this assertion object
	 */
	public MailpitContainerAssert awaitMessages() {
		isNotNull();
		MailpitClient client = actual.getClient();
		Awaitility.await().atMost(timeout).pollInterval(pollInterval).until(() -> client.getMessageCount() > 0);
		return this;
	}

	/**
	 * Waits until the mailbox contains exactly the expected number of messages.
	 * @param expectedCount the expected message count
	 * @return this assertion object
	 */
	public MailpitContainerAssert awaitMessageCount(int expectedCount) {
		isNotNull();
		MailpitClient client = actual.getClient();
		Awaitility.await()
			.atMost(timeout)
			.pollInterval(pollInterval)
			.until(() -> client.getMessageCount() == expectedCount);
		return this;
	}

	/**
	 * Returns assertions on the first message in the mailbox.
	 * @return MessageAssert for the first message
	 * @throws AssertionError if no messages exist
	 */
	public MessageAssert firstMessage() {
		isNotNull();
		List<Message> messages = actual.getClient().getAllMessages();
		if (messages.isEmpty()) {
			failWithMessage("Expected mailbox to contain at least one message but it was empty");
		}
		return new MessageAssert(messages.get(0));
	}

	/**
	 * Returns assertions on the last message in the mailbox.
	 * @return MessageAssert for the last message
	 * @throws AssertionError if no messages exist
	 */
	public MessageAssert lastMessage() {
		isNotNull();
		List<Message> messages = actual.getClient().getAllMessages();
		if (messages.isEmpty()) {
			failWithMessage("Expected mailbox to contain at least one message but it was empty");
		}
		return new MessageAssert(messages.get(messages.size() - 1));
	}

	/**
	 * Returns assertions on all messages in the mailbox.
	 * @return MessagesAssert for all messages
	 */
	public MessagesAssert messages() {
		isNotNull();
		return new MessagesAssert(actual.getClient().getAllMessages());
	}

	/**
	 * Verifies that there is a message with the given subject.
	 * @param subject the expected subject
	 * @return this assertion object
	 */
	public MailpitContainerAssert hasMessageWithSubject(String subject) {
		isNotNull();
		List<Message> messages = actual.getClient().getAllMessages();
		boolean found = messages.stream().anyMatch(m -> subject.equals(m.subject()));
		if (!found) {
			failWithMessage("Expected to find a message with subject <%s> but none was found. Found subjects: %s",
					subject, messages.stream().map(Message::subject).toList());
		}
		return this;
	}

	/**
	 * Verifies that there is a message sent to the given recipient.
	 * @param recipientAddress the expected recipient email address
	 * @return this assertion object
	 */
	public MailpitContainerAssert hasMessageTo(String recipientAddress) {
		isNotNull();
		List<Message> messages = actual.getClient().getAllMessages();
		boolean found = messages.stream()
			.anyMatch(m -> m.to().stream().anyMatch(addr -> recipientAddress.equals(addr.address())));
		if (!found) {
			failWithMessage("Expected to find a message to <%s> but none was found", recipientAddress);
		}
		return this;
	}

	/**
	 * Verifies that there is a message from the given sender.
	 * @param senderAddress the expected sender email address
	 * @return this assertion object
	 */
	public MailpitContainerAssert hasMessageFrom(String senderAddress) {
		isNotNull();
		List<Message> messages = actual.getClient().getAllMessages();
		boolean found = messages.stream().anyMatch(m -> m.from() != null && senderAddress.equals(m.from().address()));
		if (!found) {
			failWithMessage("Expected to find a message from <%s> but none was found", senderAddress);
		}
		return this;
	}

}
