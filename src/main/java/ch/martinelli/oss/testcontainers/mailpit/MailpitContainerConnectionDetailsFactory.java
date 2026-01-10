package ch.martinelli.oss.testcontainers.mailpit;

import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

/**
 * {@link ContainerConnectionDetailsFactory} to create {@link MailpitConnectionDetails}
 * from a {@link MailpitContainer}.
 */
class MailpitContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<MailpitContainer, MailpitConnectionDetails> {

	MailpitContainerConnectionDetailsFactory() {
		super("mailpit");
	}

	@Override
	protected MailpitConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<MailpitContainer> source) {
		return new MailpitContainerConnectionDetails(source);
	}

	/**
	 * {@link MailpitConnectionDetails} backed by a {@link MailpitContainer}.
	 */
	private static final class MailpitContainerConnectionDetails extends ContainerConnectionDetails<MailpitContainer>
			implements MailpitConnectionDetails {

		private MailpitContainerConnectionDetails(ContainerConnectionSource<MailpitContainer> source) {
			super(source);
		}

		@Override
		public String getHost() {
			return getContainer().getSmtpHost();
		}

		@Override
		public int getPort() {
			return getContainer().getSmtpPort();
		}

		@Override
		public String getHttpUrl() {
			return getContainer().getHttpUrl();
		}

	}

}
