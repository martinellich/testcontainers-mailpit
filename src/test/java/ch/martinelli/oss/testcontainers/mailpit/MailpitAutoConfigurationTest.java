package ch.martinelli.oss.testcontainers.mailpit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;

class MailpitAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(MailpitAutoConfiguration.class, MailSenderAutoConfiguration.class));

	@Test
	void shouldConfigureMailpitBeansByDefault() {
		this.contextRunner.withPropertyValues("spring.mail.host=smtp.example.com").run(context -> {
			assertThat(context).hasSingleBean(MailpitConnectionDetails.class);
			assertThat(context).hasSingleBean(MailpitClient.class);
			assertThat(context).hasSingleBean(JavaMailSender.class);
			JavaMailSenderImpl mailSender = context.getBean(JavaMailSenderImpl.class);
			assertThat(mailSender.getHost()).isEqualTo("localhost");
			assertThat(mailSender.getPort()).isEqualTo(1025);
		});
	}

	@Test
	void shouldConfigureMailpitBeansWhenExplicitlyEnabled() {
		this.contextRunner.withPropertyValues("mailpit.enabled=true").run(context -> {
			assertThat(context).hasSingleBean(MailpitConnectionDetails.class);
			assertThat(context).hasSingleBean(MailpitClient.class);
		});
	}

	@Test
	void shouldBackOffWhenDisabled() {
		this.contextRunner.withPropertyValues("mailpit.enabled=false").run(context -> {
			assertThat(context).doesNotHaveBean(MailpitConnectionDetails.class);
			assertThat(context).doesNotHaveBean(MailpitClient.class);
			assertThat(context).doesNotHaveBean(MailpitProperties.class);
		});
	}

	@Test
	void shouldFallBackToSpringBootMailConfigurationWhenDisabled() {
		this.contextRunner
			.withPropertyValues("mailpit.enabled=false", "spring.mail.host=smtp.example.com", "spring.mail.port=587")
			.run(context -> {
				assertThat(context).hasSingleBean(JavaMailSender.class);
				JavaMailSenderImpl mailSender = context.getBean(JavaMailSenderImpl.class);
				assertThat(mailSender.getHost()).isEqualTo("smtp.example.com");
				assertThat(mailSender.getPort()).isEqualTo(587);
			});
	}

}
