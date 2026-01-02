package com.icio.sportakuz.config.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost("smtp.gmail.com");
		sender.setPort(587);
		sender.setUsername("sportakuz1@gmail.com");
		// TODO: Move password to variables
		sender.setPassword("");

		Properties props = sender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");

		return sender;
	}
}

