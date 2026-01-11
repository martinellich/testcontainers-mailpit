package ch.martinelli.oss.testcontainers.mailpit;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class MailpitContainerTest {

	@Container
	static MailpitContainer mailpit = new MailpitContainer();

	@Test
	void shouldStartAndExposeCorrectPorts() {
		assertThat(mailpit.isRunning()).isTrue();
		assertThat(mailpit.getSmtpPort()).isPositive();
		assertThat(mailpit.getHttpPort()).isPositive();
		assertThat(mailpit.getHttpUrl()).startsWith("http://");
	}

	@Test
	void shouldCatchEmailSentViaSMTP() throws MessagingException {
		// Clear any existing messages
		MailpitClient client = mailpit.getClient();
		client.deleteAllMessages();

		// Send an email
		sendEmail("sender@example.com", "recipient@example.com", "Test Subject", "Hello, this is a test email!");

		// Verify the email was caught
		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		Message message = messages.get(0);
		assertThat(message.from().address()).isEqualTo("sender@example.com");
		assertThat(message.to()).hasSize(1);
		assertThat(message.to().get(0).address()).isEqualTo("recipient@example.com");
		assertThat(message.subject()).isEqualTo("Test Subject");

		// Verify message content
		String plainBody = client.getMessagePlain(message.id());
		assertThat(plainBody).contains("Hello, this is a test email!");
	}

	@Test
	void shouldSupportMultipleRecipients() throws MessagingException {
		MailpitClient client = mailpit.getClient();
		client.deleteAllMessages();

		// Send email to multiple recipients
		sendEmailToMultipleRecipients("sender@example.com", List.of("alice@example.com", "bob@example.com"),
				"Group Email", "This is a group email.");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		Message message = messages.get(0);
		assertThat(message.to()).hasSize(2);
		assertThat(message.to()).extracting(Address::address)
			.containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
	}

	@Test
	void shouldDeleteMessages() throws MessagingException {
		MailpitClient client = mailpit.getClient();
		client.deleteAllMessages();

		// Send two emails
		sendEmail("sender@example.com", "recipient@example.com", "Email 1", "Body 1");
		sendEmail("sender@example.com", "recipient@example.com", "Email 2", "Body 2");

		assertThat(client.getMessageCount()).isEqualTo(2);

		// Delete all messages
		client.deleteAllMessages();

		assertThat(client.getMessageCount()).isZero();
	}

	@Test
	void shouldRetrieveMessageSource() throws MessagingException {
		MailpitClient client = mailpit.getClient();
		client.deleteAllMessages();

		sendEmail("sender@example.com", "recipient@example.com", "Source Test", "Test body");

		List<Message> messages = client.getAllMessages();
		String source = client.getMessageSource(messages.get(0).id());

		assertThat(source).contains("From: sender@example.com")
			.contains("To: recipient@example.com")
			.contains("Subject: Source Test");
	}

	private void sendEmail(String from, String to, String subject, String body) throws MessagingException {
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

	private void sendEmailToMultipleRecipients(String from, List<String> recipients, String subject, String body)
			throws MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));

		for (String recipient : recipients) {
			message.addRecipient(RecipientType.TO, new InternetAddress(recipient));
		}

		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

}
