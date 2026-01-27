package ch.martinelli.oss.testcontainers.mailpit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldReturnZeroAttachmentsWhenNull() {
		Message message = createMessage(null);

		assertThat(message.attachmentCount()).isZero();
	}

	@Test
	void shouldReturnAttachmentCountFromIntNode() {
		Message message = createMessage(new IntNode(3));

		assertThat(message.attachmentCount()).isEqualTo(3);
	}

	@Test
	void shouldReturnAttachmentCountFromArrayNode() {
		ArrayNode arrayNode = objectMapper.createArrayNode();
		arrayNode.addObject().put("filename", "file1.pdf");
		arrayNode.addObject().put("filename", "file2.pdf");

		Message message = createMessage(arrayNode);

		assertThat(message.attachmentCount()).isEqualTo(2);
	}

	@Test
	void shouldReturnZeroForNonIntNonArrayNode() {
		Message message = createMessage(new TextNode("not a number"));

		assertThat(message.attachmentCount()).isZero();
	}

	@Test
	void shouldReturnRecipientsWhenToIsNotNull() {
		List<Address> to = List.of(new Address("John", "john@example.com"), new Address("Jane", "jane@example.com"));
		Message message = new Message("id", "messageId", new Address("Sender", "sender@example.com"), to, null, null,
				null, "Subject", 100, Instant.now(), false, null, "Snippet", null);

		assertThat(message.recipients()).hasSize(2);
		assertThat(message.recipients().get(0).address()).isEqualTo("john@example.com");
	}

	@Test
	void shouldReturnEmptyListWhenToIsNull() {
		Message message = new Message("id", "messageId", new Address("Sender", "sender@example.com"), null, null, null,
				null, "Subject", 100, Instant.now(), false, null, "Snippet", null);

		assertThat(message.recipients()).isNotNull();
		assertThat(message.recipients()).isEmpty();
	}

	private Message createMessage(com.fasterxml.jackson.databind.JsonNode attachmentsNode) {
		return new Message("id", "messageId", new Address("Sender", "sender@example.com"),
				Collections.singletonList(new Address("Recipient", "recipient@example.com")), null, null, null,
				"Subject", 100, Instant.now(), false, attachmentsNode, "Snippet", null);
	}

}
