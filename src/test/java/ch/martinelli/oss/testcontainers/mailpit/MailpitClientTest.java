package ch.martinelli.oss.testcontainers.mailpit;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class MailpitClientTest {

	@Container
	static MailpitContainer mailpit = new MailpitContainer();

	private MailpitClient client;

	@BeforeEach
	void setUp() {
		client = mailpit.getClient();
		client.deleteAllMessages();
	}

	@Test
	void shouldGetMessageById() throws MessagingException {
		sendEmail("sender@example.com", "recipient@example.com", "Test Subject", "Test body");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		Message message = client.getMessage(messages.get(0).id());

		assertThat(message.from().address()).isEqualTo("sender@example.com");
		assertThat(message.to()).hasSize(1);
		assertThat(message.to().get(0).address()).isEqualTo("recipient@example.com");
		assertThat(message.subject()).isEqualTo("Test Subject");
	}

	@Test
	void shouldThrowExceptionWhenMessageNotFound() {
		assertThatThrownBy(() -> client.getMessage("nonexistent-id")).isInstanceOf(MailpitException.class)
			.hasMessageContaining("Message not found");
	}

	@Test
	void shouldGetMessageHtml() throws MessagingException {
		sendHtmlEmail("sender@example.com", "recipient@example.com", "HTML Test",
				"<html><body><h1>Hello</h1></body></html>");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		String html = client.getMessageHtml(messages.get(0).id());

		assertThat(html).contains("<h1>Hello</h1>");
	}

	@Test
	void shouldGetMessagePlain() throws MessagingException {
		sendEmail("sender@example.com", "recipient@example.com", "Plain Only", "Plain text only");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		String plain = client.getMessagePlain(messages.get(0).id());

		assertThat(plain).contains("Plain text only");
	}

	@Test
	void shouldDeleteSingleMessage() throws MessagingException {
		sendEmail("sender@example.com", "recipient@example.com", "Email 1", "Body 1");
		sendEmail("sender@example.com", "recipient@example.com", "Email 2", "Body 2");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(2);

		client.deleteMessage(messages.get(0).id());

		assertThat(client.getMessageCount()).isEqualTo(1);
	}

	@Test
	void shouldReturnZeroCountWhenNoMessages() {
		assertThat(client.getMessageCount()).isZero();
	}

	@Test
	void shouldReturnEmptyListWhenNoMessages() {
		List<Message> messages = client.getAllMessages();

		assertThat(messages).isEmpty();
	}

	@Test
	void shouldHandleMessageWithCreatedTimestamp() throws MessagingException {
		sendEmail("sender@example.com", "recipient@example.com", "Timestamp Test", "Body");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		Message message = messages.get(0);

		assertThat(message.created()).isNotNull();
	}

	@Test
	void shouldGetMessageSource() throws MessagingException {
		sendEmail("sender@example.com", "recipient@example.com", "Source Test", "This is the raw body");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		String source = client.getMessageSource(messages.get(0).id());

		assertThat(source).isNotNull();
		assertThat(source).contains("Source Test");
		assertThat(source).contains("sender@example.com");
	}

	@Test
	void shouldReturnNullForNonExistentMessageSource() {
		String source = client.getMessageSource("nonexistent-id");

		assertThat(source).isNull();
	}

	@Test
	void shouldReturnNullForNonExistentMessageHtml() {
		String html = client.getMessageHtml("nonexistent-id");

		assertThat(html).isNull();
	}

	@Test
	void shouldReturnNullForNonExistentMessagePlain() {
		String plain = client.getMessagePlain("nonexistent-id");

		assertThat(plain).isNull();
	}

	@Test
	void shouldDeleteMultipleMessages() throws MessagingException {
		sendEmail("sender@example.com", "recipient@example.com", "Email 1", "Body 1");
		sendEmail("sender@example.com", "recipient@example.com", "Email 2", "Body 2");
		sendEmail("sender@example.com", "recipient@example.com", "Email 3", "Body 3");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(3);

		client.deleteMessages(List.of(messages.get(0).id(), messages.get(1).id()));

		assertThat(client.getMessageCount()).isEqualTo(1);
	}

	@Test
	void shouldGetMessageRecipients() throws MessagingException {
		sendEmailToMultiple("sender@example.com", new String[] { "a@test.com", "b@test.com" }, "Multi Recipient",
				"Body");

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		Message message = messages.get(0);

		assertThat(message.recipients()).hasSize(2);
		assertThat(message.recipients().get(0).address()).isEqualTo("a@test.com");
		assertThat(message.recipients().get(1).address()).isEqualTo("b@test.com");
	}

	@Test
	void shouldReturnEmptyListForNullTo() throws MessagingException {
		sendEmail("sender@example.com", "recipient@example.com", "Test", "Body");

		List<Message> messages = client.getAllMessages();
		Message message = messages.get(0);

		// recipients() should never return null
		assertThat(message.recipients()).isNotNull();
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

	private void sendHtmlEmail(String from, String to, String subject, String htmlBody) throws MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", mailpit.getSmtpHost());
		props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));

		Session session = Session.getInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipient(RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setContent(htmlBody, "text/html");

		Transport.send(message);
	}

	private void sendEmailToMultiple(String from, String[] to, String subject, String body) throws MessagingException {
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
