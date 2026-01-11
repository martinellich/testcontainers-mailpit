package ch.martinelli.oss.testcontainers.mailpit.assertions;

import ch.martinelli.oss.testcontainers.mailpit.MailpitContainer;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static ch.martinelli.oss.testcontainers.mailpit.assertions.MailpitAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class MailpitAssertionsTest {

	@Container
	static MailpitContainer mailpit = new MailpitContainer();

	@BeforeEach
	void setUp() {
		mailpit.getClient().deleteAllMessages();
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
		void shouldAssertHasMessages() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).hasMessages();
		}

		@Test
		void shouldAssertMessageCount() throws Exception {
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
		void shouldFailWhenExpectingNoMessagesButSome() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).hasNoMessages()).isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected mailbox to be empty");
		}

		@Test
		void shouldAssertHasMessageWithSubject() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Welcome Email", "Body");

			assertThat(mailpit).hasMessageWithSubject("Welcome Email");
		}

		@Test
		void shouldAssertHasMessageTo() throws Exception {
			sendEmail("sender@test.com", "john@example.com", "Test", "Body");

			assertThat(mailpit).hasMessageTo("john@example.com");
		}

		@Test
		void shouldAssertHasMessageFrom() throws Exception {
			sendEmail("noreply@company.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).hasMessageFrom("noreply@company.com");
		}

		@Test
		void shouldGetFirstMessage() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "First Email", "Body");

			assertThat(mailpit).firstMessage().hasSubject("First Email");
		}

		@Test
		void shouldGetLastMessage() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "First Email", "Body 1");
			sendEmail("sender@test.com", "recipient@test.com", "Second Email", "Body 2");

			assertThat(mailpit).lastMessage().hasSubject("First Email");
		}

		@Test
		void shouldGetAllMessages() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().hasSize(2);
		}

	}

	@Nested
	class MessageAwaiterTests {

		@Test
		void shouldAwaitMessage() throws Exception {
			new Thread(() -> {
				try {
					Thread.sleep(500);
					sendEmail("sender@test.com", "recipient@test.com", "Async Test", "Body");
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).start();

			assertThat(mailpit).withTimeout(Duration.ofSeconds(5)).awaitMessage().withSubject("Async Test").isPresent();
		}

		@Test
		void shouldAwaitMessageWithFilters() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Async Test", "Body");

			assertThat(mailpit).awaitMessage()
				.withSubject("Async Test")
				.from("sender@test.com")
				.to("recipient@test.com")
				.isPresent();
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
		void shouldFailWhenAssertingAbsentButPresent() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Existing", "Body");

			assertThatThrownBy(() -> assertThat(mailpit).awaitMessage().withSubject("Existing").isAbsent())
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Expected no matching message");
		}

		@Test
		void shouldAwaitMessageCount() throws Exception {
			new Thread(() -> {
				try {
					Thread.sleep(200);
					sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");
					Thread.sleep(200);
					sendEmail("sender@test.com", "recipient@test.com", "Email 2", "Body");
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).start();

			assertThat(mailpit).withTimeout(Duration.ofSeconds(5)).awaitMessageCount(2);
		}

	}

	@Nested
	class MessageAssertTests {

		@Test
		void shouldAssertSubject() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Welcome", "Body");

			assertThat(mailpit).firstMessage().hasSubject("Welcome");
		}

		@Test
		void shouldAssertSubjectContaining() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Order Confirmation #12345", "Body");

			assertThat(mailpit).firstMessage().hasSubjectContaining("Confirmation");
		}

		@Test
		void shouldAssertFrom() throws Exception {
			sendEmail("noreply@example.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().isFrom("noreply@example.com");
		}

		@Test
		void shouldAssertRecipient() throws Exception {
			sendEmail("sender@test.com", "john@example.com", "Test", "Body");

			assertThat(mailpit).firstMessage().hasRecipient("john@example.com");
		}

		@Test
		void shouldAssertRecipientCount() throws Exception {
			sendEmailToMultiple("sender@test.com", new String[] { "a@test.com", "b@test.com" }, "Test", "Body");

			assertThat(mailpit).firstMessage().hasRecipientCount(2);
		}

		@Test
		void shouldAssertNoAttachments() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().hasNoAttachments();
		}

		@Test
		void shouldAssertUnread() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().isUnread();
		}

		@Test
		void shouldAssertCreatedAfter() throws Exception {
			Instant before = Instant.now().minusSeconds(1);
			sendEmail("sender@test.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage().wasCreatedAfter(before);
		}

		@Test
		void shouldAssertSnippetContaining() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Test", "This is the email body content");

			assertThat(mailpit).firstMessage().hasSnippetContaining("email body");
		}

	}

	@Nested
	class MessagesAssertTests {

		@Test
		void shouldFilterBySubject() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Newsletter", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Invoice", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Newsletter", "Body");

			assertThat(mailpit).messages().filteredOnSubject("Newsletter").hasSize(2);
		}

		@Test
		void shouldFilterBySender() throws Exception {
			sendEmail("newsletter@company.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("support@company.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().filteredOnSender("newsletter@company.com").hasSize(1);
		}

		@Test
		void shouldFilterByRecipient() throws Exception {
			sendEmail("sender@test.com", "alice@test.com", "Email 1", "Body");
			sendEmail("sender@test.com", "bob@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().filteredOnRecipient("alice@test.com").hasSize(1);
		}

		@Test
		void shouldAssertAllMatch() throws Exception {
			sendEmail("newsletter@company.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("newsletter@company.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().allAreFrom("newsletter@company.com");
		}

		@Test
		void shouldAssertContainsMessageWithSubject() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Welcome", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Goodbye", "Body");

			assertThat(mailpit).messages().containsMessageWithSubject("Welcome");
		}

		@Test
		void shouldGetFirstAndLast() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "First", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Second", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Third", "Body");

			assertThat(mailpit).messages().first().hasSubject("Third");
			assertThat(mailpit).messages().last().hasSubject("First");
		}

		@Test
		void shouldAllMessagesSatisfy() throws Exception {
			sendEmail("sender@test.com", "recipient@test.com", "Email 1", "Body");
			sendEmail("sender@test.com", "recipient@test.com", "Email 2", "Body");

			assertThat(mailpit).messages().allMessagesSatisfy(msg -> msg.isFrom("sender@test.com").isUnread());
		}

	}

	@Nested
	class AddressAssertTests {

		@Test
		void shouldAssertAddressViaMessage() throws Exception {
			sendEmailWithName("John Doe", "john@example.com", "recipient@test.com", "Test", "Body");

			assertThat(mailpit).firstMessage()
				.fromAddress()
				.hasAddress("john@example.com")
				.hasName("John Doe")
				.hasDisplayName()
				.isInDomain("example.com");
		}

	}

	private void sendEmail(String from, String to, String subject, String body) throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipient(RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

	private void sendEmailWithName(String fromName, String fromAddress, String to, String subject, String body)
			throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromAddress, fromName));
		message.setRecipient(RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

	private void sendEmailToMultiple(String from, String[] to, String subject, String body) throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));

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

}
