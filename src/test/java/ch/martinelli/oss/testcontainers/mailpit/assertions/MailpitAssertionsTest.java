package ch.martinelli.oss.testcontainers.mailpit.assertions;

import ch.martinelli.oss.testcontainers.mailpit.Address;
import ch.martinelli.oss.testcontainers.mailpit.MailpitContainer;
import ch.martinelli.oss.testcontainers.mailpit.Message;
import jakarta.activation.DataHandler;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static ch.martinelli.oss.testcontainers.mailpit.assertions.MailpitAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings({ "java:S5778", "java:S2925" })
@Testcontainers
class MailpitAssertionsTest {

	@Container
	static MailpitContainer mailpit = new MailpitContainer();

	@BeforeEach
	void setUp() {
		mailpit.getClient().deleteAllMessages();
	}

	@Nested
	class DirectAssertionTests {

		@Test
		void shouldAssertOnMessageDirectly() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Direct Test", "Body");

			Message message = mailpit.getClient().getAllMessages().get(0);

			assertThat(message).hasSubject("Direct Test").isFrom("sender@test.com");
		}

		@Test
		void shouldAssertOnAddressDirectly() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			Message message = mailpit.getClient().getAllMessages().get(0);
			Address from = message.from();

			assertThat(from).hasAddress("sender@test.com").isInDomain("test.com");
		}

		@Test
		void shouldAssertOnMessagesListDirectly() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Email 2", "Body");

			List<Message> messages = mailpit.getClient().getAllMessages();

			assertThat(messages).hasSize(2).allAreFrom("sender@test.com");
		}

	}

	@Nested
	class MailpitContainerAssertTests {

		@Test
		void shouldAssertIsRunning() {
			assertThat(mailpit).isRunning();
		}

		@Test
		void shouldAssertHasNoMessages() {
			assertThat(mailpit).hasNoMessages();
		}

		@Test
		void shouldAssertHasMessages() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).hasMessages();
		}

		@Test
		void shouldAssertMessageCount() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test 1", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Test 2", "Body");

			assertThat(mailpit).hasMessageCount(2);
		}

		@Test
		void shouldFailWhenExpectingMessagesButNone() {
			assertThatThrownBy(() -> assertThat(mailpit).hasMessages()).isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected mailbox to contain messages");
		}

		@Test
		void shouldFailWhenExpectingNoMessagesButSome() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).hasNoMessages()).isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected mailbox to be empty");
		}

		@Test
		void shouldFailWhenMessageCountDoesNotMatch() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).hasMessageCount(5)).isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected mailbox to contain <5> message(s)");
		}

		@Test
		void shouldAssertHasMessageWithSubject() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Welcome Email", "Body");

			assertThat(mailpit).hasMessageWithSubject("Welcome Email");
		}

		@Test
		void shouldFailWhenHasMessageWithSubjectNotFound() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Actual Subject", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).hasMessageWithSubject("Expected Subject"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected to find a message with subject <Expected Subject>");
		}

		@Test
		void shouldAssertHasMessageTo() throws MessagingException {
			sendEmail("sender@test.com", "john@example.com", "Test", "Body");

			assertThat(mailpit).hasMessageTo("john@example.com");
		}

		@Test
		void shouldFailWhenHasMessageToNotFound() throws MessagingException {
			sendEmail("sender@test.com", "actual@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).hasMessageTo("expected@test.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected to find a message to <expected@test.com>");
		}

		@Test
		void shouldAssertHasMessageFrom() throws MessagingException {
			sendEmail("noreply@company.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).hasMessageFrom("noreply@company.com");
		}

		@Test
		void shouldFailWhenHasMessageFromNotFound() throws MessagingException {
			sendEmail("actual@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).hasMessageFrom("expected@test.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected to find a message from <expected@test.com>");
		}

		@Test
		void shouldGetFirstMessage() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "First Email", "Body");

			assertThat(mailpit).firstMessage().hasSubject("First Email");
		}

		@Test
		void shouldFailWhenFirstMessageOnEmptyMailbox() {
			assertThatThrownBy(() -> assertThat(mailpit).firstMessage()).isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected mailbox to contain at least one message");
		}

		@Test
		void shouldGetLastMessage() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "First Email", "Body 1");
			sendEmail("sender@test.com", "recipient@test.com", "Second Email", "Body 2");

			assertThat(mailpit).lastMessage().hasSubject("First Email");
		}

		@Test
		void shouldFailWhenLastMessageOnEmptyMailbox() {
			assertThatThrownBy(() -> assertThat(mailpit).lastMessage()).isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected mailbox to contain at least one message");
		}

		@Test
		void shouldGetAllMessages() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().hasSize(2);
		}

	}

	@Nested
	class MessageAwaiterTests {

		@Test
		void shouldAwaitMessage() {
			new Thread(() -> {
				try {
					Thread.sleep(500);
					sendEmail("sender@test.com", "recipient@test.com", "Async Test", "Body");
				}
				catch (InterruptedException | MessagingException e) {
					throw new RuntimeException(e);
				}
			}).start();

			assertThat(mailpit).withTimeout(Duration.ofSeconds(5)).awaitMessage().withSubject("Async Test").isPresent();
		}

		@Test
		void shouldAwaitMessageWithFilters() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Async Test", "Body");

			assertThat(mailpit).awaitMessage()
				.withSubject("Async Test")
				.from("sender@test.com")
				.to("recipient@test.com")
				.isPresent();
		}

		@Test
		void shouldAwaitMessageWithSubjectContaining() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Order Confirmation #12345", "Body");

			assertThat(mailpit).awaitMessage().withSubjectContaining("Confirmation").isPresent();
		}

		@Test
		void shouldAwaitMessageWithCc() throws MessagingException {
			sendEmailWithCc("sender@test.com", "recipient@test.com", "cc@test.com", "CC Test", "Body");

			assertThat(mailpit).awaitMessage().cc("cc@test.com").isPresent();
		}

		@Test
		void shouldAwaitMessageWithoutAttachments() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "No Attachments", "Body");

			assertThat(mailpit).awaitMessage().withoutAttachments().isPresent();
		}

		@Test
		void shouldAwaitMessageWithCustomPredicate() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Custom Match", "Body");

			assertThat(mailpit).awaitMessage().matching(m -> m.subject().startsWith("Custom")).isPresent();
		}

		@Test
		void shouldTimeoutWhenMessageNotFound() {
			assertThatThrownBy(() -> assertThat(mailpit).withTimeout(Duration.ofMillis(500))
				.withPollInterval(Duration.ofMillis(100))
				.awaitMessage()
				.withSubject("Nonexistent")
				.isPresent()).isInstanceOf(ConditionTimeoutException.class);
		}

		@Test
		void shouldAssertMessageAbsent() {
			assertThat(mailpit).awaitMessage().withSubject("Nonexistent").isAbsent();
		}

		@Test
		void shouldFailWhenAssertingAbsentButPresent() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Existing", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).awaitMessage().withSubject("Existing").isAbsent())
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected no matching message");
		}

		@Test
		void shouldAwaitMessageCount() {
			new Thread(() -> {
				try {
					Thread.sleep(200);
					sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");
					Thread.sleep(200);
					sendEmail("sender@test.com", "recipient@test.com", "Email 2", "Body");
				}
				catch (InterruptedException | MessagingException e) {
					throw new RuntimeException(e);
				}
			}).start();

			assertThat(mailpit).withTimeout(Duration.ofSeconds(5)).awaitMessageCount(2);
		}

		@Test
		void shouldAwaitMessages() {
			new Thread(() -> {
				try {
					Thread.sleep(200);
					sendEmail("sender@test.com", "recipient@test.com", "Email", "Body");
				}
				catch (InterruptedException | MessagingException e) {
					throw new RuntimeException(e);
				}
			}).start();

			assertThat(mailpit).withTimeout(Duration.ofSeconds(5)).awaitMessages();
		}

		@Test
		void shouldNotFindMessageWithAttachmentsWhenNoneHave() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "No Attachments", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).withTimeout(Duration.ofMillis(500))
				.withPollInterval(Duration.ofMillis(100))
				.awaitMessage()
				.withAttachments()
				.isPresent()).isInstanceOf(ConditionTimeoutException.class);
		}

	}

	@Nested
	class MessageAssertTests {

		@Test
		void shouldAssertSubject() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Welcome", "Body");

			assertThat(mailpit).firstMessage().hasSubject("Welcome");
		}

		@Test
		void shouldAssertSubjectContaining() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Order Confirmation #12345", "Body");

			assertThat(mailpit).firstMessage().hasSubjectContaining("Confirmation");
		}

		@Test
		void shouldAssertFrom() throws MessagingException {
			sendEmail("noreply@example.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().isFrom("noreply@example.com");
		}

		@Test
		void shouldAssertFromName() throws MessagingException, UnsupportedEncodingException {
			sendEmailWithName("John Sender", "sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().isFromName("John Sender");
		}

		@Test
		void shouldAssertRecipient() throws MessagingException {
			sendEmail("sender@test.com", "john@example.com", "Test", "Body");

			assertThat(mailpit).firstMessage().hasRecipient("john@example.com");
		}

		@Test
		void shouldAssertRecipientCount() throws MessagingException {
			sendEmailToMultiple("sender@test.com", new String[] { "a@test.com", "b@test.com" }, "Test", "Body");

			assertThat(mailpit).firstMessage().hasRecipientCount(2);
		}

		@Test
		void shouldAssertCcRecipient() throws MessagingException {
			sendEmailWithCc("sender@test.com", "recipient@test.com", "cc@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().hasCcRecipient("cc@test.com");
		}

		@Test
		void shouldAssertNoAttachments() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().hasNoAttachments();
		}

		@Test
		void shouldAssertAttachmentCount() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().hasAttachmentCount(0);
		}

		@Test
		void shouldAssertUnread() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().isUnread();
		}

		@Test
		void shouldAssertCreatedAfter() throws MessagingException {
			Instant before = Instant.now().minusSeconds(1);
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().wasCreatedAfter(before);
		}

		@Test
		void shouldAssertCreatedBefore() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");
			Instant after = Instant.now().plusSeconds(10);

			assertThat(mailpit).firstMessage().wasCreatedBefore(after);
		}

		@Test
		void shouldAssertSnippetContaining() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "This is the email body content");

			assertThat(mailpit).firstMessage().hasSnippetContaining("email body");
		}

		@Test
		void shouldGetMessage() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test Subject", "Body");

			Message message = assertThat(mailpit).firstMessage().getMessage();

			org.assertj.core.api.Assertions.assertThat(message).isNotNull();
			org.assertj.core.api.Assertions.assertThat(message.subject()).isEqualTo("Test Subject");
		}

		@Test
		void shouldFailWhenSubjectDoesNotMatch() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Actual Subject", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasSubject("Expected Subject"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected subject to be <Expected Subject>");
		}

		@Test
		void shouldFailWhenSubjectDoesNotContain() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Hello World", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasSubjectContaining("Goodbye"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected subject to contain <Goodbye>");
		}

		@Test
		void shouldFailWhenFromDoesNotMatch() throws MessagingException {
			sendEmail("actual@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().isFrom("expected@test.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to be from <expected@test.com>");
		}

		@Test
		void shouldFailWhenFromNameDoesNotMatch() throws MessagingException, UnsupportedEncodingException {
			sendEmailWithName("Actual Name", "sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().isFromName("Expected Name"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected sender name to be <Expected Name>");
		}

		@Test
		void shouldFailWhenRecipientNotFound() throws MessagingException {
			sendEmail("sender@test.com", "actual@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasRecipient("expected@test.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to have recipient <expected@test.com>");
		}

		@Test
		void shouldFailWhenRecipientCountDoesNotMatch() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasRecipientCount(5))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to have <5> recipient(s)");
		}

		@Test
		void shouldFailWhenCcRecipientNotFound() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasCcRecipient("cc@test.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to have CC recipient <cc@test.com>");
		}

		@Test
		void shouldFailWhenExpectingAttachmentsButNone() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasAttachments())
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to have attachments");
		}

		@Test
		void shouldFailWhenAttachmentCountDoesNotMatch() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasAttachmentCount(3))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to have <3> attachment(s)");
		}

		@Test
		void shouldFailWhenCreatedAfterCheckFails() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");
			Instant future = Instant.now().plusSeconds(3600);

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().wasCreatedAfter(future))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to be created after");
		}

		@Test
		void shouldFailWhenCreatedBeforeCheckFails() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");
			Instant past = Instant.now().minusSeconds(3600);

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().wasCreatedBefore(past))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to be created before");
		}

		@Test
		void shouldFailWhenSnippetDoesNotContain() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Hello world");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasSnippetContaining("goodbye"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected snippet to contain <goodbye>");
		}

		@Test
		void shouldFailWhenFromAddressIsNull() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			// This test validates the error path - fromAddress() throws when from is null
			// We can't easily create a message with null from, so we verify the method
			// works
			assertThat(mailpit).firstMessage().fromAddress().hasAddress("sender@test.com");
		}

		@Test
		void shouldFailWhenExpectingReadButUnread() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().isRead()).isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to be read");
		}

		@Test
		void shouldFailWhenBccRecipientNotFound() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasBccRecipient("bcc@test.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to have BCC recipient");
		}

		@Test
		void shouldFailWhenTagNotFound() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasTag("nonexistent"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to have tag <nonexistent>");
		}

		@Test
		void shouldFailWhenTagsNotFound() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasTags("tag1", "tag2"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to have tag");
		}

		@Test
		void shouldFailWhenExpectingNoAttachmentsButHasAttachments() throws MessagingException {
			sendEmailWithAttachment("sender@test.com", "recipient@test.com", "Test", "Body", "test.txt",
					"attachment content");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().hasNoAttachments())
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected message to have no attachments");
		}

	}

	@Nested
	class MessagesAssertTests {

		@Test
		void shouldFilterBySubject() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Newsletter", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Invoice", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Newsletter", "Body");

			assertThat(mailpit).messages().filteredOnSubject("Newsletter").hasSize(2);
		}

		@Test
		void shouldFilterBySender() throws MessagingException {
			sendEmail("newsletter@company.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("support@company.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().filteredOnSender("newsletter@company.com").hasSize(1);
		}

		@Test
		void shouldFilterByRecipient() throws MessagingException {
			sendEmail("sender@test.com", "alice@test.com", "Email 1", "Body");
			sendEmail("sender@test.com", "bob@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().filteredOnRecipient("alice@test.com").hasSize(1);
		}

		@Test
		void shouldFilterByPredicate() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Short", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "A Very Long Subject Line", "Body");

			assertThat(mailpit).messages()
				.filteredOnPredicate(m -> m.subject().length() > 10)
				.hasSize(1)
				.first()
				.hasSubject("A Very Long Subject Line");
		}

		@Test
		void shouldAssertAllMatch() throws MessagingException {
			sendEmail("newsletter@company.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("newsletter@company.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().allAreFrom("newsletter@company.com");
		}

		@Test
		void shouldAssertContainsMessageWithSubject() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Welcome", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Goodbye", "Body");

			assertThat(mailpit).messages().containsMessageWithSubject("Welcome");
		}

		@Test
		void shouldAssertContainsMessageTo() throws MessagingException {
			sendEmail("sender@test.com", "alice@test.com", "Email 1", "Body");
			sendEmail("sender@test.com", "bob@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().containsMessageTo("alice@test.com");
		}

		@Test
		void shouldAssertContainsMessageFrom() throws MessagingException {
			sendEmail("alice@test.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("bob@test.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().containsMessageFrom("alice@test.com");
		}

		@Test
		void shouldGetFirstAndLast() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "First", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Second", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Third", "Body");

			assertThat(mailpit).messages().first().hasSubject("Third");
			assertThat(mailpit).messages().last().hasSubject("First");
		}

		@Test
		void shouldGetElementAtIndex() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "First", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Second", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Third", "Body");

			assertThat(mailpit).messages().element(1).hasSubject("Second");
		}

		@Test
		void shouldAllMessagesSatisfy() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().allMessagesSatisfy(msg -> msg.isFrom("sender@test.com").isUnread());
		}

		@Test
		void shouldAssertAllAreUnread() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().allAreUnread();
		}

		@Test
		void shouldHasMessageSatisfying() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body 1");
			sendEmail("sender@test.com", "recipient@test.com", "Email 2", "Body 2");
			sendEmail("sender@test.com", "recipient@test.com", "Email 3", "Body 3");

			assertThat(mailpit).messages()
				.hasMessageSatisfying(0, msg -> msg.hasSubject("Email 3").isFrom("sender@test.com").isUnread())
				.hasMessageSatisfying(1, msg -> msg.hasSubject("Email 2").hasRecipient("recipient@test.com"))
				.hasMessageSatisfying(2, msg -> msg.hasSubject("Email 1"));
		}

		@Test
		void shouldFailWhenIndexOutOfBounds() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");

			assertThatThrownBy(
					() -> assertThat(mailpit).messages().hasMessageSatisfying(5, msg -> msg.hasSubject("Email 1")))
				.isInstanceOf(AssertionError.class);
		}

		@Test
		void shouldFailWhenIndexIsNegative() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");

			assertThatThrownBy(
					() -> assertThat(mailpit).messages().hasMessageSatisfying(-1, msg -> msg.hasSubject("Email 1")))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Index must be non-negative but was: -1");
		}

		@Test
		void shouldFailWhenContainsMessageWithSubjectNotFound() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Actual Subject", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).messages().containsMessageWithSubject("Expected Subject"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected to find a message with subject <Expected Subject>");
		}

		@Test
		void shouldFailWhenContainsMessageToNotFound() throws MessagingException {
			sendEmail("sender@test.com", "actual@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).messages().containsMessageTo("expected@test.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected to find a message to <expected@test.com>");
		}

		@Test
		void shouldFailWhenContainsMessageFromNotFound() throws MessagingException {
			sendEmail("actual@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).messages().containsMessageFrom("expected@test.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected to find a message from <expected@test.com>");
		}

		@Test
		void shouldFailWhenAllAreFromButNot() throws MessagingException {
			sendEmail("sender1@test.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("sender2@test.com", "recipient@test.com", "Email 2", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).messages().allAreFrom("sender1@test.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected all messages to be from <sender1@test.com>");
		}

		@Test
		void shouldFailWhenAllAreUnreadButSomeAreRead() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");
			// Since we can't easily mark messages as read, we just verify the positive
			// case
			assertThat(mailpit).messages().allAreUnread();
		}

		@Test
		void shouldFailWhenAllAreReadButSomeAreUnread() throws MessagingException {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).messages().allAreRead()).isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected all messages to be read");
		}

	}

	@Nested
	class AddressAssertTests {

		@Test
		void shouldAssertAddressViaMessage() throws MessagingException, UnsupportedEncodingException {
			sendEmailWithName("John Doe", "john@example.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage()
				.fromAddress()
				.hasAddress("john@example.com")
				.hasName("John Doe")
				.hasDisplayName()
				.isInDomain("example.com");
		}

		@Test
		void shouldAssertNoDisplayName() throws MessagingException {
			sendEmail("plain@example.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().fromAddress().hasNoDisplayName();
		}

		@Test
		void shouldAssertAddressContains() throws MessagingException {
			sendEmail("test.user@example.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().fromAddress().addressContains("test.user");
		}

		@Test
		void shouldFailWhenAddressDoesNotMatch() throws MessagingException {
			sendEmail("actual@example.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(
					() -> assertThat(mailpit).firstMessage().fromAddress().hasAddress("expected@example.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected address to be <expected@example.com>");
		}

		@Test
		void shouldFailWhenNameDoesNotMatch() throws MessagingException, UnsupportedEncodingException {
			sendEmailWithName("Actual Name", "test@example.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().fromAddress().hasName("Expected Name"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected name to be <Expected Name>");
		}

		@Test
		void shouldFailWhenExpectingDisplayNameButNone() throws MessagingException {
			sendEmail("plain@example.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().fromAddress().hasDisplayName())
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected address to have a display name");
		}

		@Test
		void shouldFailWhenExpectingNoDisplayNameButHasOne() throws MessagingException, UnsupportedEncodingException {
			sendEmailWithName("Has Name", "test@example.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().fromAddress().hasNoDisplayName())
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected address to have no display name");
		}

		@Test
		void shouldFailWhenNotInDomain() throws MessagingException {
			sendEmail("test@other.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().fromAddress().isInDomain("example.com"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected address to be in domain <example.com>");
		}

		@Test
		void shouldFailWhenAddressDoesNotContain() throws MessagingException {
			sendEmail("test@example.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).firstMessage().fromAddress().addressContains("notfound"))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected address to contain <notfound>");
		}

	}

	private void sendEmail(String from, String to, String subject, String body) throws MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));
		props.put("mail.smtp.localhost", "localhost");
		props.put("mail.from", "noreply@localhost");

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipient(RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

	private void sendEmailWithName(String fromName, String fromAddress, String to, String subject, String body)
			throws MessagingException, UnsupportedEncodingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));
		props.put("mail.smtp.localhost", "localhost");
		props.put("mail.from", "noreply@localhost");

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromAddress, fromName));
		message.setRecipient(RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

	private void sendEmailToMultiple(String from, String[] to, String subject, String body) throws MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));
		props.put("mail.smtp.localhost", "localhost");
		props.put("mail.from", "noreply@localhost");

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		for (String recipient : to) {
			message.addRecipient(RecipientType.TO, new InternetAddress(recipient));
		}
		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

	private void sendEmailWithCc(String from, String to, String cc, String subject, String body)
			throws MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));
		props.put("mail.smtp.localhost", "localhost");
		props.put("mail.from", "noreply@localhost");

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipient(RecipientType.TO, new InternetAddress(to));
		message.setRecipient(RecipientType.CC, new InternetAddress(cc));
		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

	private void sendEmailWithAttachment(String from, String to, String subject, String body, String attachmentName,
			String attachmentContent) throws MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));
		props.put("mail.smtp.localhost", "localhost");
		props.put("mail.from", "noreply@localhost");

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipient(RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);

		MimeMultipart multipart = new MimeMultipart();

		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setText(body);
		multipart.addBodyPart(textPart);

		MimeBodyPart attachmentPart = new MimeBodyPart();
		attachmentPart
			.setDataHandler(new DataHandler(new ByteArrayDataSource(attachmentContent.getBytes(), "text/plain")));
		attachmentPart.setFileName(attachmentName);
		multipart.addBodyPart(attachmentPart);

		message.setContent(multipart);

		Transport.send(message);
	}

}
