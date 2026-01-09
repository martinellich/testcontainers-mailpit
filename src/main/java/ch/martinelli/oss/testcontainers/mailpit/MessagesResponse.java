package ch.martinelli.oss.testcontainers.mailpit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the response from the Mailpit messages list API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record MessagesResponse(@JsonProperty("messages") List<Message> messages, @JsonProperty("total") int total,
		@JsonProperty("unread") int unread, @JsonProperty("count") int count, @JsonProperty("start") int start,
		@JsonProperty("tags") List<String> tags) {
}
