package ch.martinelli.oss.testcontainers.mailpit;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MailpitServiceConnectionTest {

	@Autowired
	MailpitClient client;

	@Autowired
	MailpitContainer mailpit;

	@Autowired
	MailpitConnectionDetails connectionDetails;

	@Autowired
	JavaMailSender javaMailSender;

	@BeforeEach
	void setUp() {
		client.deleteAllMessages();
	}

	@Test
	void clientShouldBeAutowiredWithDynamicPorts() {
		assertThat(client).isNotNull();
	}

	@Test
	void connectionDetailsShouldUseDynamicPorts() {
		// Connection details should match the running container's dynamic ports
		assertThat(connectionDetails.getHost()).isEqualTo(mailpit.getHost());
		assertThat(connectionDetails.getPort()).isEqualTo(mailpit.getSmtpPort());
		assertThat(connectionDetails.getHttpUrl()).isEqualTo(mailpit.getHttpUrl());
	}

	@Test
	void autowiredClientShouldWorkWithDynamicPorts() throws Exception {
		// Send an email using the container's dynamic SMTP port
		sendEmail("sender@example.com", "recipient@example.com", "Service Connection Test", "Testing autowired client");

		// The autowired client should be able to retrieve the message
		// using the dynamic HTTP port from the container
		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);

		Message message = messages.get(0);
		assertThat(message.subject()).isEqualTo("Service Connection Test");
	}

	@Test
	void javaMailSenderShouldBeAutoConfigured() {
		assertThat(javaMailSender).isNotNull();
	}

	@Test
	void javaMailSenderShouldSendEmail() {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("sender@example.com");
		message.setTo("recipient@example.com");
		message.setSubject("JavaMailSender Test");
		message.setText("Testing auto-configured JavaMailSender");

		javaMailSender.send(message);

		List<Message> messages = client.getAllMessages();
		assertThat(messages).hasSize(1);
		assertThat(messages.get(0).subject()).isEqualTo("JavaMailSender Test");
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

}
