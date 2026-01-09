package ch.martinelli.oss.testcontainers.mailpit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Represents an email message from Mailpit.
 * <p>
 * This record handles both message summaries (from list endpoint) and detailed messages
 * (from single message endpoint). The attachments field can be either an integer count
 * (in summaries) or an array of attachment objects (in detailed view).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(@JsonProperty("ID") String id, @JsonProperty("MessageID") String messageId,
		@JsonProperty("From") Address from, @JsonProperty("To") List<Address> to, @JsonProperty("Cc") List<Address> cc,
		@JsonProperty("Bcc") List<Address> bcc, @JsonProperty("ReplyTo") List<Address> replyTo,
		@JsonProperty("Subject") String subject, @JsonProperty("Size") int size,
		@JsonProperty("Created") Instant created, @JsonProperty("Read") boolean read,
		@JsonProperty("Attachments") JsonNode attachmentsNode, @JsonProperty("Snippet") String snippet,
		@JsonProperty("Tags") List<String> tags) {

	/**
	 * Returns the number of attachments.
	 * @return attachment count
	 */
	public int attachmentCount() {
		if (attachmentsNode == null) {
			return 0;
		}
		if (attachmentsNode.isInt()) {
			return attachmentsNode.asInt();
		}
		if (attachmentsNode.isArray()) {
			return attachmentsNode.size();
		}
		return 0;
	}

	/**
	 * Returns the list of recipients as a convenience method.
	 * @return list of recipient addresses, never null
	 */
	public List<Address> recipients() {
		return to != null ? to : Collections.emptyList();
	}

}
