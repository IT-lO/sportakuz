package com.icio.sportakuz.bookings.controllers;

import com.icio.sportakuz.classes.domain.Booking;
import com.icio.sportakuz.classes.repo.BookingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/my/bookings")
public class MyBookingsController {

	private final BookingRepository bookingRepository;
	private final ZoneId zone = ZoneId.of("Europe/Warsaw");

	public MyBookingsController(BookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}

	/** GET /my/bookings – główny widok zarezerwowanych zajęć. */
	@GetMapping
	public String calendarRoot(Model model) {
		model.addAttribute("pageTitle", "Moje rezerwacje");

		// Placeholder until users get added
		String userName = "zzz";

		List<Booking> bookings =  bookingRepository.findAllByUserName(userName);
		List<MyBookingDto> dtoList = bookings.stream().map(this::toDto).collect(Collectors.toList());
		model.addAttribute("bookings", dtoList);
		return "bookings/my_bookings";
	}

	private MyBookingDto toDto(Booking b) {
		var startZoned = b.getClazz().getStartTime().atZoneSameInstant(zone);
		var endZoned = b.getClazz().getEndTime().atZoneSameInstant(zone);
		String date = startZoned.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
		String time = startZoned.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
		int duration = (int) java.time.Duration.between(startZoned.toOffsetDateTime(), endZoned.toOffsetDateTime()).toMinutes();


		return new MyBookingDto(
			b.getId(),
			b.getClazz().getType() != null ? b.getClazz().getType().getName() : "Zajęcia",
			b.getClazz().getInstructor() != null ? (b.getClazz().getInstructor().getFirstName() + " " + b.getClazz().getInstructor().getLastName()) : "Instruktor",
			date,
			time,
			duration,
			b.getClazz().getRoom().getName()
			);
	}

	public record MyBookingDto(
			Long id,
			String activityName,
			String instructor,
			String date,
			String time,
			int duration,
			String room
	) {}
}