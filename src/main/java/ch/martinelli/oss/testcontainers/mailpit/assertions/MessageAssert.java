package ch.martinelli.oss.testcontainers.mailpit.assertions;

import ch.martinelli.oss.testcontainers.mailpit.Address;
import ch.martinelli.oss.testcontainers.mailpit.Message;
import org.assertj.core.api.AbstractAssert;

import java.time.Instant;
import java.util.List;

/**
 * AssertJ assertions for {@link Message}.
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * assertThat(message)
 *     .hasSubject("Welcome")
 *     .isFrom("noreply@example.com")
 *     .hasRecipient("user@example.com")
 *     .hasNoAttachments();
 * }</pre>
 */
public class MessageAssert extends AbstractAssert<MessageAssert, Message> {

	public MessageAssert(Message actual) {
		super(actual, MessageAssert.class);
	}

	/**
	 * Verifies the message subject matches exactly.
	 * @param expectedSubject the expected subject
	 * @return this assertion object
	 */
	public MessageAssert hasSubject(String expectedSubject) {
		isNotNull();
		if (!expectedSubject.equals(actual.subject())) {
			failWithMessage("Expected subject to be <%s> but was <%s>", expectedSubject, actual.subject());
		}
		return this;
	}

	/**
	 * Verifies the message subject contains the given substring.
	 * @param substring the expected substring
	 * @return this assertion object
	 */
	public MessageAssert hasSubjectContaining(String substring) {
		isNotNull();
		if (actual.subject() == null || !actual.subject().contains(substring)) {
			failWithMessage("Expected subject to contain <%s> but was <%s>", substring, actual.subject());
		}
		return this;
	}

	/**
	 * Verifies the sender email address.
	 * @param expectedAddress the expected sender email address
	 * @return this assertion object
	 */
	public MessageAssert isFrom(String expectedAddress) {
		isNotNull();
		if (actual.from() == null || !expectedAddress.equals(actual.from().address())) {
			failWithMessage("Expected message to be from <%s> but was from <%s>", expectedAddress,
					actual.from() != null ? actual.from().address() : "null");
		}
		return this;
	}

	/**
	 * Verifies the sender has the expected display name.
	 * @param expectedName the expected sender name
	 * @return this assertion object
	 */
	public MessageAssert isFromName(String expectedName) {
		isNotNull();
		if (actual.from() == null || !expectedName.equals(actual.from().name())) {
			failWithMessage("Expected sender name to be <%s> but was <%s>", expectedName,
					actual.from() != null ? actual.from().name() : "null");
		}
		return this;
	}

	/**
	 * Verifies the message has the given recipient.
	 * @param expectedAddress the expected recipient email address
	 * @return this assertion object
	 */
	public MessageAssert hasRecipient(String expectedAddress) {
		isNotNull();
		boolean found = actual.to().stream().anyMatch(addr -> expectedAddress.equals(addr.address()));
		if (!found) {
			failWithMessage("Expected message to have recipient <%s> but recipients were %s", expectedAddress,
					actual.to().stream().map(Address::address).toList());
		}
		return this;
	}

	/**
	 * Verifies the message has exactly the expected number of recipients (To).
	 * @param expectedCount the expected recipient count
	 * @return this assertion object
	 */
	public MessageAssert hasRecipientCount(int expectedCount) {
		isNotNull();
		int actualCount = actual.to() != null ? actual.to().size() : 0;
		if (actualCount != expectedCount) {
			failWithMessage("Expected message to have <%d> recipient(s) but had <%d>", expectedCount, actualCount);
		}
		return this;
	}

	/**
	 * Verifies the message has the given CC recipient.
	 * @param expectedAddress the expected CC recipient email address
	 * @return this assertion object
	 */
	public MessageAssert hasCcRecipient(String expectedAddress) {
		isNotNull();
		List<Address> cc = actual.cc();
		boolean found = cc != null && cc.stream().anyMatch(addr -> expectedAddress.equals(addr.address()));
		if (!found) {
			failWithMessage("Expected message to have CC recipient <%s> but CC recipients were %s", expectedAddress,
					cc != null ? cc.stream().map(Address::address).toList() : "empty");
		}
		return this;
	}

	/**
	 * Verifies the message has the given BCC recipient.
	 * @param expectedAddress the expected BCC recipient email address
	 * @return this assertion object
	 */
	public MessageAssert hasBccRecipient(String expectedAddress) {
		isNotNull();
		List<Address> bcc = actual.bcc();
		boolean found = bcc != null && bcc.stream().anyMatch(addr -> expectedAddress.equals(addr.address()));
		if (!found) {
			failWithMessage("Expected message to have BCC recipient <%s>", expectedAddress);
		}
		return this;
	}

	/**
	 * Verifies the message has at least one attachment.
	 * @return this assertion object
	 */
	public MessageAssert hasAttachments() {
		isNotNull();
		if (actual.attachmentCount() == 0) {
			failWithMessage("Expected message to have attachments but it had none");
		}
		return this;
	}

	/**
	 * Verifies the message has no attachments.
	 * @return this assertion object
	 */
	public MessageAssert hasNoAttachments() {
		isNotNull();
		if (actual.attachmentCount() > 0) {
			failWithMessage("Expected message to have no attachments but had <%d>", actual.attachmentCount());
		}
		return this;
	}

	/**
	 * Verifies the message has exactly the expected number of attachments.
	 * @param expectedCount the expected attachment count
	 * @return this assertion object
	 */
	public MessageAssert hasAttachmentCount(int expectedCount) {
		isNotNull();
		int actualCount = actual.attachmentCount();
		if (actualCount != expectedCount) {
			failWithMessage("Expected message to have <%d> attachment(s) but had <%d>", expectedCount, actualCount);
		}
		return this;
	}

	/**
	 * Verifies the message has been read.
	 * @return this assertion object
	 */
	public MessageAssert isRead() {
		isNotNull();
		if (!actual.read()) {
			failWithMessage("Expected message to be read but it was unread");
		}
		return this;
	}

	/**
	 * Verifies the message is unread.
	 * @return this assertion object
	 */
	public MessageAssert isUnread() {
		isNotNull();
		if (actual.read()) {
			failWithMessage("Expected message to be unread but it was read");
		}
		return this;
	}

	/**
	 * Verifies the message was created after the given instant.
	 * @param instant the instant to compare against
	 * @return this assertion object
	 */
	public MessageAssert wasCreatedAfter(Instant instant) {
		isNotNull();
		if (actual.created() == null || !actual.created().isAfter(instant)) {
			failWithMessage("Expected message to be created after <%s> but was created at <%s>", instant,
					actual.created());
		}
		return this;
	}

	/**
	 * Verifies the message was created before the given instant.
	 * @param instant the instant to compare against
	 * @return this assertion object
	 */
	public MessageAssert wasCreatedBefore(Instant instant) {
		isNotNull();
		if (actual.created() == null || !actual.created().isBefore(instant)) {
			failWithMessage("Expected message to be created before <%s> but was created at <%s>", instant,
					actual.created());
		}
		return this;
	}

	/**
	 * Verifies the message snippet contains the given text.
	 * @param text the expected text
	 * @return this assertion object
	 */
	public MessageAssert hasSnippetContaining(String text) {
		isNotNull();
		if (actual.snippet() == null || !actual.snippet().contains(text)) {
			failWithMessage("Expected snippet to contain <%s> but was <%s>", text, actual.snippet());
		}
		return this;
	}

	/**
	 * Verifies the message has the given tag.
	 * @param tag the expected tag
	 * @return this assertion object
	 */
	public MessageAssert hasTag(String tag) {
		isNotNull();
		List<String> tags = actual.tags();
		if (tags == null || !tags.contains(tag)) {
			failWithMessage("Expected message to have tag <%s> but tags were %s", tag, tags);
		}
		return this;
	}

	/**
	 * Verifies the message has all the given tags.
	 * @param expectedTags the expected tags
	 * @return this assertion object
	 */
	public MessageAssert hasTags(String... expectedTags) {
		isNotNull();
		List<String> tags = actual.tags();
		for (String tag : expectedTags) {
			if (tags == null || !tags.contains(tag)) {
				failWithMessage("Expected message to have tag <%s> but tags were %s", tag, tags);
			}
		}
		return this;
	}

	/**
	 * Returns the underlying message for further inspection.
	 * @return the message being asserted
	 */
	public Message getMessage() {
		return actual;
	}

	/**
	 * Returns assertions for the sender address.
	 * @return AddressAssert for the sender
	 * @throws AssertionError if no sender is set
	 */
	public AddressAssert fromAddress() {
		isNotNull();
		if (actual.from() == null) {
			failWithMessage("Expected message to have a sender but it was null");
		}
		return new AddressAssert(actual.from());
	}

}
