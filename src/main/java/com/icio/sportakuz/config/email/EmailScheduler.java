package com.icio.sportakuz.config.email;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailScheduler {

	private final EmailService emailService;

	public EmailScheduler(EmailService emailService) {
		this.emailService = emailService;
	}

	@Scheduled(cron = "30 43 13 * * ?", zone = "Europe/Warsaw")
	public void sendReminderEmail() {
		emailService.sendReminderEmails();
	}
}