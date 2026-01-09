package ch.martinelli.oss.testcontainers.mailpit;

/**
 * Exception thrown when an error occurs while interacting with Mailpit.
 */
public class MailpitException extends RuntimeException {

	public MailpitException(String message) {
		super(message);
	}

	public MailpitException(String message, Throwable cause) {
		super(message, cause);
	}

}
