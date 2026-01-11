package ch.martinelli.oss.testcontainers.mailpit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Client for interacting with the Mailpit REST API.
 * <p>
 * Provides methods to retrieve, inspect, and delete caught email messages.
 *
 * @see <a href="https://mailpit.axllent.org/docs/api-v1/">Mailpit API Documentation</a>
 */
public class MailpitClient {

	private static final String APPLICATION_JSON = "application/json";

	private static final String PATH = "/api/v1/messages";

	private final String baseUrl;

	private final HttpClient httpClient;

	private final ObjectMapper objectMapper;

	public MailpitClient(String baseUrl) {
		this.baseUrl = baseUrl;
		this.httpClient = HttpClient.newHttpClient();
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
	}

	/**
	 * Retrieves all messages from Mailpit.
	 * @return a list of all caught messages
	 * @throws MailpitException if an error occurs while fetching messages
	 */
	public List<Message> getAllMessages() {
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + PATH))
				.header("Accept", APPLICATION_JSON)
				.GET()
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new MailpitException("Failed to fetch messages: HTTP " + response.statusCode());
			}

			MessagesResponse messagesResponse = objectMapper.readValue(response.body(), MessagesResponse.class);
			return messagesResponse.messages() != null ? messagesResponse.messages() : List.of();
		}
		catch (IOException | InterruptedException e) {
			throw new MailpitException("Failed to fetch messages", e);
		}
	}

	/**
	 * Returns the number of messages in Mailpit.
	 * @return the message count
	 * @throws MailpitException if an error occurs
	 */
	public int getMessageCount() {
		return getAllMessages().size();
	}

	/**
	 * Retrieves a specific message by ID.
	 * @param id the message ID
	 * @return the message details
	 * @throws MailpitException if an error occurs or the message is not found
	 */
	public Message getMessage(String id) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/api/v1/message/" + id))
				.header("Accept", APPLICATION_JSON)
				.GET()
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 404) {
				throw new MailpitException("Message not found: " + id);
			}
			if (response.statusCode() != 200) {
				throw new MailpitException("Failed to fetch message: HTTP " + response.statusCode());
			}

			return objectMapper.readValue(response.body(), Message.class);
		}
		catch (IOException | InterruptedException e) {
			throw new MailpitException("Failed to fetch message", e);
		}
	}

	/**
	 * Retrieves the HTML body of a message.
	 * @param id the message ID
	 * @return the HTML body, or null if not available
	 * @throws MailpitException if an error occurs
	 */
	public String getMessageHtml(String id) {
		return fetchViewPart(id, "html");
	}

	/**
	 * Retrieves the plain text body of a message.
	 * @param id the message ID
	 * @return the plain text body, or null if not available
	 * @throws MailpitException if an error occurs
	 */
	public String getMessagePlain(String id) {
		return fetchViewPart(id, "txt");
	}

	/**
	 * Retrieves the raw source of a message.
	 * @param id the message ID
	 * @return the raw message source
	 * @throws MailpitException if an error occurs
	 */
	public String getMessageSource(String id) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/api/v1/message/" + id + "/raw"))
				.GET()
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 404) {
				return null;
			}
			if (response.statusCode() != 200) {
				throw new MailpitException("Failed to fetch message source: HTTP " + response.statusCode());
			}

			return response.body();
		}
		catch (IOException | InterruptedException e) {
			throw new MailpitException("Failed to fetch message source", e);
		}
	}

	private String fetchViewPart(String id, String part) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/view/" + id + "." + part))
				.GET()
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 404) {
				return null;
			}
			if (response.statusCode() != 200) {
				throw new MailpitException("Failed to fetch message part: HTTP " + response.statusCode());
			}

			return response.body();
		}
		catch (IOException | InterruptedException e) {
			throw new MailpitException("Failed to fetch message part", e);
		}
	}

	/**
	 * Deletes all messages from Mailpit.
	 * @throws MailpitException if an error occurs
	 */
	public void deleteAllMessages() {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + PATH)).DELETE().build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200 && response.statusCode() != 204) {
				throw new MailpitException("Failed to delete messages: HTTP " + response.statusCode());
			}
		}
		catch (IOException | InterruptedException e) {
			throw new MailpitException("Failed to delete messages", e);
		}
	}

	/**
	 * Deletes specific messages by their IDs.
	 * @param ids the message IDs to delete
	 * @throws MailpitException if an error occurs
	 */
	public void deleteMessages(List<String> ids) {
		try {
			String jsonBody = objectMapper.writeValueAsString(new DeleteRequest(ids));

			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + PATH))
				.header("Content-Type", APPLICATION_JSON)
				.method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody))
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200 && response.statusCode() != 204) {
				throw new MailpitException("Failed to delete messages: HTTP " + response.statusCode());
			}
		}
		catch (IOException | InterruptedException e) {
			throw new MailpitException("Failed to delete messages", e);
		}
	}

	/**
	 * Deletes a specific message.
	 * @param id the message ID to delete
	 * @throws MailpitException if an error occurs
	 */
	public void deleteMessage(String id) {
		deleteMessages(List.of(id));
	}

	private record DeleteRequest(@com.fasterxml.jackson.annotation.JsonProperty("ids") List<String> ids) {
	}

}
