package ch.martinelli.oss.testcontainers.mailpit.assertions;

import ch.martinelli.oss.testcontainers.mailpit.MailpitClient;
import ch.martinelli.oss.testcontainers.mailpit.Message;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Fluent builder for awaiting and asserting on messages.
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * assertThat(mailpitContainer)
 *     .awaitMessage()
 *     .withSubject("Welcome")
 *     .from("noreply@example.com")
 *     .to("user@example.com")
 *     .isPresent();
 * }</pre>
 */
public class MessageAwaiter {

	private final MailpitClient client;

	private final Duration timeout;

	private final Duration pollInterval;

	private Predicate<Message> predicate = m -> true;

	public MessageAwaiter(MailpitClient client, Duration timeout, Duration pollInterval) {
		this.client = client;
		this.timeout = timeout;
		this.pollInterval = pollInterval;
	}

	/**
	 * Filters messages by exact subject match.
	 * @param subject the expected subject
	 * @return this awaiter
	 */
	public MessageAwaiter withSubject(String subject) {
		this.predicate = this.predicate.and(m -> subject.equals(m.subject()));
		return this;
	}

	/**
	 * Filters messages by subject containing a substring.
	 * @param substring the substring to search for
	 * @return this awaiter
	 */
	public MessageAwaiter withSubjectContaining(String substring) {
		this.predicate = this.predicate.and(m -> m.subject() != null && m.subject().contains(substring));
		return this;
	}

	/**
	 * Filters messages by sender address.
	 * @param senderAddress the expected sender email address
	 * @return this awaiter
	 */
	public MessageAwaiter from(String senderAddress) {
		this.predicate = this.predicate.and(m -> m.from() != null && senderAddress.equals(m.from().address()));
		return this;
	}

	/**
	 * Filters messages by recipient address.
	 * @param recipientAddress the expected recipient email address
	 * @return this awaiter
	 */
	public MessageAwaiter to(String recipientAddress) {
		this.predicate = this.predicate
			.and(m -> m.to().stream().anyMatch(addr -> recipientAddress.equals(addr.address())));
		return this;
	}

	/**
	 * Filters messages by CC recipient address.
	 * @param ccAddress the expected CC email address
	 * @return this awaiter
	 */
	public MessageAwaiter cc(String ccAddress) {
		this.predicate = this.predicate
			.and(m -> m.cc() != null && m.cc().stream().anyMatch(addr -> ccAddress.equals(addr.address())));
		return this;
	}

	/**
	 * Filters messages that have attachments.
	 * @return this awaiter
	 */
	public MessageAwaiter withAttachments() {
		this.predicate = this.predicate.and(m -> m.attachmentCount() > 0);
		return this;
	}

	/**
	 * Filters messages that have no attachments.
	 * @return this awaiter
	 */
	public MessageAwaiter withoutAttachments() {
		this.predicate = this.predicate.and(m -> m.attachmentCount() == 0);
		return this;
	}

	/**
	 * Filters messages by a custom predicate.
	 * @param customPredicate the predicate to apply
	 * @return this awaiter
	 */
	public MessageAwaiter matching(Predicate<Message> customPredicate) {
		this.predicate = this.predicate.and(customPredicate);
		return this;
	}

	/**
	 * Waits for a matching message and returns assertions for it.
	 * @return MessageAssert for the found message
	 * @throws org.awaitility.core.ConditionTimeoutException if no matching message is
	 * found within the timeout
	 */
	public MessageAssert isPresent() {
		Message found = Awaitility.await()
			.atMost(timeout)
			.pollInterval(pollInterval)
			.until(this::findMatchingMessage, Optional::isPresent)
			.orElseThrow();
		return new MessageAssert(found);
	}

	/**
	 * Asserts that no matching message exists (does not wait).
	 * @throws AssertionError if a matching message is found
	 */
	public void isAbsent() {
		Optional<Message> found = findMatchingMessage();
		if (found.isPresent()) {
			throw new AssertionError(
					"Expected no matching message but found one with subject: " + found.get().subject());
		}
	}

	private Optional<Message> findMatchingMessage() {
		return client.getAllMessages().stream().filter(predicate).findFirst();
	}

}
