# Testcontainers Mailpit

A [Testcontainers](https://www.testcontainers.org/) module for [Mailpit](https://mailpit.axllent.org/) - an email and SMTP testing tool with API for developers.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>ch.martinelli.oss</groupId>
    <artifactId>testcontainers-mailpit</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Basic Usage

```java
@Testcontainers
class EmailServiceTest {

    @Container
    static MailpitContainer mailpit = new MailpitContainer();

    @Test
    void shouldSendEmail() throws Exception {
        // Configure your mail sender
        Properties props = new Properties();
        props.put("mail.smtp.host", mailpit.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("sender@example.com"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("recipient@example.com"));
        message.setSubject("Test Subject");
        message.setText("Hello, this is a test email!");

        Transport.send(message);

        // Verify the email was caught
        MailpitClient client = mailpit.getClient();
        List<Message> messages = client.getAllMessages();

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).subject()).isEqualTo("Test Subject");
    }
}
```

### Container Configuration

The `MailpitContainer` exposes two ports:

| Port | Description                              |
|------|------------------------------------------|
| 1025 | SMTP port for sending emails             |
| 8025 | HTTP port for web interface and REST API |

```java
// Create container with default image (axllent/mailpit:latest)
MailpitContainer mailpit = new MailpitContainer();

// Or specify a custom image version
MailpitContainer mailpit = new MailpitContainer("axllent/mailpit:v1.21");

// Get connection details
String smtpHost = mailpit.getSmtpHost();
int smtpPort = mailpit.getSmtpPort();
int httpPort = mailpit.getHttpPort();
String httpUrl = mailpit.getHttpUrl(); // e.g., "http://localhost:32789"
```

### MailpitClient API

The `MailpitClient` provides methods to interact with caught emails:

```java
MailpitClient client = mailpit.getClient();

// Get all messages
List<Message> messages = client.getAllMessages();

// Get message count
int count = client.getMessageCount();

// Get a specific message by ID
Message message = client.getMessage("abc123");

// Get message content
String html = client.getMessageHtml("abc123");    // HTML body
String plain = client.getMessagePlain("abc123");  // Plain text body
String source = client.getMessageSource("abc123"); // Raw email source

// Delete messages
client.deleteMessage("abc123");       // Delete specific message
client.deleteMessages(List.of("id1", "id2")); // Delete multiple messages
client.deleteAllMessages();           // Delete all messages
```

### Message Properties

The `Message` record contains the following properties:

| Property      | Type            | Description                              |
|---------------|-----------------|------------------------------------------|
| `id`          | `String`        | Unique message identifier                |
| `messageId`   | `String`        | Email Message-ID header                  |
| `from`        | `Address`       | Sender address (name + email)            |
| `to`          | `List<Address>` | List of TO recipients                    |
| `cc`          | `List<Address>` | List of CC recipients                    |
| `bcc`         | `List<Address>` | List of BCC recipients                   |
| `replyTo`     | `List<Address>` | Reply-To addresses                       |
| `subject`     | `String`        | Email subject                            |
| `size`        | `int`           | Message size in bytes                    |
| `created`     | `Instant`       | Timestamp when the message was received  |
| `read`        | `boolean`       | Whether the message has been read        |
| `snippet`     | `String`        | Preview of message content (up to 250 chars) |
| `tags`        | `List<String>`  | Tags applied to the message              |

The `Message` record also provides the following methods:

| Method              | Return Type     | Description                              |
|---------------------|-----------------|------------------------------------------|
| `attachmentCount()` | `int`           | Returns the number of attachments        |
| `recipients()`      | `List<Address>` | Convenience method returning TO recipients |

The `Address` record contains:

| Property  | Type     | Description           |
|-----------|----------|-----------------------|
| `name`    | `String` | Display name (optional) |
| `address` | `String` | Email address         |

## Requirements

- Java 17 or higher
- Docker

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
