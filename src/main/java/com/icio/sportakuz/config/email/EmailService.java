package com.icio.sportakuz.config.email;

import com.icio.sportakuz.entity.Activity;
import com.icio.sportakuz.repo.ActivityRepository;
import com.icio.sportakuz.entity.Booking;
import com.icio.sportakuz.repo.BookingRepository;
import com.icio.sportakuz.repo.BookingStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final ActivityRepository activityRepository;
	private final BookingRepository bookingRepository;

	public EmailService(JavaMailSender mailSender, ActivityRepository activityRepository, BookingRepository bookingRepository) {
		this.mailSender = mailSender;
		this.activityRepository = activityRepository;
		this.bookingRepository = bookingRepository;
	}

	public void sendReminderEmails() {

		ZoneId zone = ZoneId.of("Europe/Warsaw");
		ZonedDateTime now = ZonedDateTime.now(zone);
		ZonedDateTime startOfTomorrow = now.plusDays(1).toLocalDate().atStartOfDay(zone);
		ZonedDateTime endOfTomorrow = startOfTomorrow.plusDays(1);

		OffsetDateTime from = startOfTomorrow.toOffsetDateTime();
		OffsetDateTime to = endOfTomorrow.toOffsetDateTime();

		List<Activity> activities = activityRepository.findByStartTimeBetween(from, to);

		DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

		for (Activity activity : activities) {
			List<BookingStatus> activeStatuses = List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED, BookingStatus.PAID);
			List<Booking> bookings = bookingRepository.findAllByActivity_IdAndStatusIn(activity.getId(), activeStatuses);

			Set<String> recipientEmails = bookings.stream()
					.map(Booking::getUserName)
					.filter(e -> e != null)
					.map(String::trim)
					.filter(e -> !e.isEmpty())
					.map(String::toLowerCase)
					.collect(Collectors.toSet());

			if (recipientEmails.isEmpty()) {
				continue;
			}

			String activityName = activity.getType() != null ? activity.getType().getActivityName() : "Activity";
			String instructorName = activity.getInstructor() != null ?
					(activity.getInstructor().getFirstName() + " " + activity.getInstructor().getLastName()) : "Instructor";
			String roomName = activity.getRoom() != null ? activity.getRoom().getName() : "Room";

			String localStart = activity.getStartTime().atZoneSameInstant(zone).format(timeFmt);

			for (String email : recipientEmails) {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setTo(email);
				message.setSubject("SPORTAKUZ - Reservation reminder for tomorrow");
				message.setText("Hello,\n\nThis is a reminder that tomorrow you have " + activityName + " at " + localStart + "CET with " + instructorName + " in " + roomName + ".\n\nRegards");

				mailSender.send(message);
			}
		}

	}
}

