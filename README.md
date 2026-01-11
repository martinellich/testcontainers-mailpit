# Testcontainers Mailpit

A [Testcontainers](https://www.testcontainers.org/) module for [Mailpit](https://mailpit.axllent.org/) - an email and
SMTP testing tool with API for developers.

## Installation

Add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>ch.martinelli.oss</groupId>
    <artifactId>testcontainers-mailpit</artifactId>
    <version>1.0.1</version>
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
    void shouldSendEmail() throws MessagingException {
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

### Spring Boot ServiceConnection

For Spring Boot 3.1+ applications, you can use the `@ServiceConnection` annotation for automatic configuration. This
eliminates the need to manually configure connection properties.

```java

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MailpitContainer mailpitContainer() {
        return new MailpitContainer();
    }
}
```

```java

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EmailServiceTest {

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    MailpitClient client;

    @Test
    void shouldSendAndVerifyEmail() {
        // Use the auto-configured JavaMailSender
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sender@example.com");
        message.setTo("recipient@example.com");
        message.setSubject("Test Subject");
        message.setText("Hello, this is a test email!");

        mailSender.send(message);

        // Verify using the auto-configured MailpitClient
        List<Message> messages = client.getAllMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).subject()).isEqualTo("Test Subject");
    }
}
```

When using `@ServiceConnection`, the following beans are automatically configured:

| Bean                       | Description                                           |
|----------------------------|-------------------------------------------------------|
| `JavaMailSender`           | Pre-configured Spring mail sender for sending emails  |
| `MailpitClient`            | Pre-configured client for the Mailpit REST API        |
| `MailpitConnectionDetails` | Connection details with host, SMTP port, and HTTP URL |

You can also configure Mailpit via properties when not using Testcontainers:

```properties
mailpit.host=localhost
mailpit.port=1025
mailpit.http-url=http://localhost:8025
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
client.

deleteMessage("abc123");       // Delete specific message
client.

deleteMessages(List.of("id1", "id2")); // Delete multiple messages
        client.

deleteAllMessages();           // Delete all messages
```

### AssertJ Assertions

The library provides fluent AssertJ-style assertions for testing emails without directly using the `MailpitClient`.

#### Dependencies

To use the assertions, add AssertJ to your project. For async waiting support, also add Awaitility:

```xml
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.27.6</version>
    <scope>test</scope>
</dependency>

<!-- Optional: For async email waiting -->
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <version>4.2.2</version>
    <scope>test</scope>
</dependency>
```

#### Basic Assertions

```java
import static ch.martinelli.oss.testcontainers.mailpit.assertions.MailpitAssertions.assertThat;

@Test
void shouldVerifyEmailSent() {
    // Send email...

    // Assert on the container
    assertThat(mailpit)
        .hasMessages()
        .hasMessageCount(1)
        .hasMessageWithSubject("Welcome")
        .hasMessageTo("user@example.com")
        .hasMessageFrom("noreply@myapp.com");
}
```

#### Message Assertions

```java
@Test
void shouldVerifyMessageDetails() {
    // Send email...

    assertThat(mailpit)
        .firstMessage()
        .hasSubject("Order Confirmation")
        .hasSubjectContaining("Order")
        .isFrom("orders@shop.com")
        .hasRecipient("customer@example.com")
        .hasRecipientCount(1)
        .hasNoAttachments()
        .isUnread()
        .hasSnippetContaining("Thank you for your order");
}
```

#### Waiting for Async Emails

Use Awaitility integration to wait for emails that are sent asynchronously:

```java
@Test
void shouldWaitForEmail() {
    // Trigger async email sending...

    assertThat(mailpit)
        .withTimeout(Duration.ofSeconds(30))
        .withPollInterval(Duration.ofSeconds(1))
        .awaitMessage()
        .withSubject("Password Reset")
        .from("noreply@myapp.com")
        .to("user@example.com")
        .isPresent()
        .hasSnippetContaining("Click here to reset");
}

@Test
void shouldWaitForMultipleEmails() {
    // Trigger async email sending...

    assertThat(mailpit)
        .withTimeout(Duration.ofSeconds(10))
        .awaitMessageCount(3);
}
```

#### Filtering and Collection Assertions

```java
@Test
void shouldFilterMessages() {
    // Send multiple emails...

    assertThat(mailpit)
        .messages()
        .hasSize(5)
        .filteredOnSubject("Newsletter")
        .hasSize(2)
        .allAreFrom("newsletter@company.com")
        .allAreUnread();

    // Filter by sender
    assertThat(mailpit)
        .messages()
        .filteredOnSender("support@company.com")
        .hasSize(1);

    // Filter by recipient
    assertThat(mailpit)
        .messages()
        .filteredOnRecipient("admin@example.com")
        .hasSize(3);

    // Custom assertions on each message
    assertThat(mailpit)
        .messages()
        .allMessagesSatisfy(msg -> msg
            .isFrom("noreply@company.com")
            .hasNoAttachments());
}
```

#### Address Assertions

```java
@Test
void shouldVerifyAddress() {
    // Send email...

    assertThat(mailpit)
        .firstMessage()
        .fromAddress()
        .hasAddress("support@company.com")
        .hasName("Company Support")
        .hasDisplayName()
        .isInDomain("company.com");
}
```

#### Asserting Absence of Messages

```java
@Test
void shouldVerifyNoMatchingEmail() {
    // No emails sent to this address

    assertThat(mailpit)
        .awaitMessage()
        .to("unknown@example.com")
        .isAbsent();
}
```

### Message Properties

The `Message` record contains the following properties:

| Property    | Type            | Description                                  |
|-------------|-----------------|----------------------------------------------|
| `id`        | `String`        | Unique message identifier                    |
| `messageId` | `String`        | Email Message-ID header                      |
| `from`      | `Address`       | Sender address (name + email)                |
| `to`        | `List<Address>` | List of TO recipients                        |
| `cc`        | `List<Address>` | List of CC recipients                        |
| `bcc`       | `List<Address>` | List of BCC recipients                       |
| `replyTo`   | `List<Address>` | Reply-To addresses                           |
| `subject`   | `String`        | Email subject                                |
| `size`      | `int`           | Message size in bytes                        |
| `created`   | `Instant`       | Timestamp when the message was received      |
| `read`      | `boolean`       | Whether the message has been read            |
| `snippet`   | `String`        | Preview of message content (up to 250 chars) |
| `tags`      | `List<String>`  | Tags applied to the message                  |

The `Message` record also provides the following methods:

| Method              | Return Type     | Description                                |
|---------------------|-----------------|--------------------------------------------|
| `attachmentCount()` | `int`           | Returns the number of attachments          |
| `recipients()`      | `List<Address>` | Convenience method returning TO recipients |

The `Address` record contains:

| Property  | Type     | Description             |
|-----------|----------|-------------------------|
| `name`    | `String` | Display name (optional) |
| `address` | `String` | Email address           |

## Requirements

- Java 17 or higher
- Docker

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
