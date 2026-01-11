package ch.martinelli.oss.testcontainers.mailpit.assertions;

import ch.martinelli.oss.testcontainers.mailpit.Message;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * AssertJ assertions for a list of {@link Message}s.
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * assertThat(messages)
 *     .hasSize(3)
 *     .containsMessageWithSubject("Welcome")
 *     .filteredOnSubject("Newsletter")
 *     .hasSize(2);
 * }</pre>
 */
public class MessagesAssert extends AbstractIterableAssert<MessagesAssert, List<Message>, Message, MessageAssert> {

	public MessagesAssert(List<Message> actual) {
		super(actual, MessagesAssert.class);
	}

	@Override
	protected MessageAssert toAssert(Message value, String description) {
		return new MessageAssert(value).as(description);
	}

	@Override
	protected MessagesAssert newAbstractIterableAssert(Iterable<? extends Message> iterable) {
		return new MessagesAssert((List<Message>) iterable);
	}

	/**
	 * Verifies that the list contains a message with the given subject.
	 * @param subject the expected subject
	 * @return this assertion object
	 */
	public MessagesAssert containsMessageWithSubject(String subject) {
		isNotNull();
		boolean found = actual.stream().anyMatch(m -> subject.equals(m.subject()));
		if (!found) {
			failWithMessage("Expected to find a message with subject <%s> but found subjects: %s", subject,
					actual.stream().map(Message::subject).toList());
		}
		return this;
	}

	/**
	 * Verifies that the list contains a message to the given recipient.
	 * @param recipientAddress the expected recipient email address
	 * @return this assertion object
	 */
	public MessagesAssert containsMessageTo(String recipientAddress) {
		isNotNull();
		boolean found = actual.stream()
			.anyMatch(m -> m.to().stream().anyMatch(addr -> recipientAddress.equals(addr.address())));
		if (!found) {
			failWithMessage("Expected to find a message to <%s> but none was found", recipientAddress);
		}
		return this;
	}

	/**
	 * Verifies that the list contains a message from the given sender.
	 * @param senderAddress the expected sender email address
	 * @return this assertion object
	 */
	public MessagesAssert containsMessageFrom(String senderAddress) {
		isNotNull();
		boolean found = actual.stream().anyMatch(m -> m.from() != null && senderAddress.equals(m.from().address()));
		if (!found) {
			failWithMessage("Expected to find a message from <%s> but none was found", senderAddress);
		}
		return this;
	}

	/**
	 * Filters messages by subject and returns a new MessagesAssert.
	 * @param subject the subject to filter by
	 * @return new MessagesAssert with filtered messages
	 */
	public MessagesAssert filteredOnSubject(String subject) {
		isNotNull();
		List<Message> filtered = actual.stream().filter(m -> subject.equals(m.subject())).toList();
		return new MessagesAssert(filtered);
	}

	/**
	 * Filters messages by sender and returns a new MessagesAssert.
	 * @param senderAddress the sender email address to filter by
	 * @return new MessagesAssert with filtered messages
	 */
	public MessagesAssert filteredOnSender(String senderAddress) {
		isNotNull();
		List<Message> filtered = actual.stream()
			.filter(m -> m.from() != null && senderAddress.equals(m.from().address()))
			.toList();
		return new MessagesAssert(filtered);
	}

	/**
	 * Filters messages by recipient and returns a new MessagesAssert.
	 * @param recipientAddress the recipient email address to filter by
	 * @return new MessagesAssert with filtered messages
	 */
	public MessagesAssert filteredOnRecipient(String recipientAddress) {
		isNotNull();
		List<Message> filtered = actual.stream()
			.filter(m -> m.to().stream().anyMatch(addr -> recipientAddress.equals(addr.address())))
			.toList();
		return new MessagesAssert(filtered);
	}

	/**
	 * Filters messages using a predicate and returns a new MessagesAssert.
	 * @param predicate the filter predicate
	 * @return new MessagesAssert with filtered messages
	 */
	public MessagesAssert filteredOnPredicate(Predicate<Message> predicate) {
		isNotNull();
		List<Message> filtered = actual.stream().filter(predicate).toList();
		return new MessagesAssert(filtered);
	}

	/**
	 * Returns assertions on the first message.
	 * @return MessageAssert for the first message
	 * @throws AssertionError if the list is empty
	 */
	@Override
	public MessageAssert first() {
		isNotNull();
		Assertions.assertThat(actual).isNotEmpty();
		return new MessageAssert(actual.get(0));
	}

	/**
	 * Returns assertions on the last message.
	 * @return MessageAssert for the last message
	 * @throws AssertionError if the list is empty
	 */
	@Override
	public MessageAssert last() {
		isNotNull();
		Assertions.assertThat(actual).isNotEmpty();
		return new MessageAssert(actual.get(actual.size() - 1));
	}

	/**
	 * Returns assertions on a message at the specified index.
	 * @param index the index of the message
	 * @return MessageAssert for the message at the index
	 * @throws AssertionError if the index is out of bounds
	 */
	@Override
	public MessageAssert element(int index) {
		isNotNull();
		Assertions.assertThat(actual).hasSizeGreaterThan(index);
		return new MessageAssert(actual.get(index));
	}

	/**
	 * Applies MessageAssert assertions on each message.
	 * @param assertion the assertion to apply
	 * @return this assertion object
	 */
	public MessagesAssert allMessagesSatisfy(Consumer<MessageAssert> assertion) {
		isNotNull();
		for (Message message : actual) {
			assertion.accept(new MessageAssert(message));
		}
		return this;
	}

	/**
	 * Applies MessageAssert assertions on a message at the specified index.
	 * @param index the index of the message
	 * @param assertion the assertion to apply
	 * @return this assertion object
	 * @throws AssertionError if the index is out of bounds
	 */
	public MessagesAssert hasMessageSatisfying(int index, Consumer<MessageAssert> assertion) {
		isNotNull();
		Assertions.assertThat(actual).hasSizeGreaterThan(index);
		assertion.accept(new MessageAssert(actual.get(index)));
		return this;
	}

	/**
	 * Verifies that all messages are from the given sender.
	 * @param senderAddress the expected sender email address
	 * @return this assertion object
	 */
	public MessagesAssert allAreFrom(String senderAddress) {
		isNotNull();
		for (Message message : actual) {
			if (message.from() == null || !senderAddress.equals(message.from().address())) {
				failWithMessage("Expected all messages to be from <%s> but found one from <%s>", senderAddress,
						message.from() != null ? message.from().address() : "null");
			}
		}
		return this;
	}

	/**
	 * Verifies that all messages are unread.
	 * @return this assertion object
	 */
	public MessagesAssert allAreUnread() {
		isNotNull();
		for (Message message : actual) {
			if (message.read()) {
				failWithMessage("Expected all messages to be unread but found a read message: %s", message.subject());
			}
		}
		return this;
	}

	/**
	 * Verifies that all messages are read.
	 * @return this assertion object
	 */
	public MessagesAssert allAreRead() {
		isNotNull();
		for (Message message : actual) {
			if (!message.read()) {
				failWithMessage("Expected all messages to be read but found an unread message: %s", message.subject());
			}
		}
		return this;
	}

}
