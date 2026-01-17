package com.icio.sportakuz.controller.booking;

import com.icio.sportakuz.entity.Booking;
import com.icio.sportakuz.repo.BookingRepository;
import com.icio.sportakuz.dto.booking.MyBookingDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kontroler widoku rezerwacji. Ładuje wystąpienia zajęć z bazy i udostępnia je
 * jako DTO serializowane przez Thymeleaf do JS.
 */
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
	public String calendarRoot(Model model, Principal principal) {
		model.addAttribute("pageTitle", "Moje rezerwacje");

		// Placeholder until users get added
		String userName = principal.getName();

		List<Booking> bookings =  bookingRepository.findAllByUserNameAndActivity_EndTimeAfter(userName, OffsetDateTime.now());
		List<MyBookingDto> dtoList = bookings.stream().map(this::toDto).collect(Collectors.toList());
		model.addAttribute("bookings", dtoList);
		return "bookings/my_bookings";
	}

	private MyBookingDto toDto(Booking b) {
		var startZoned = b.getActivity().getStartTime().atZoneSameInstant(zone);
		var endZoned = b.getActivity().getEndTime().atZoneSameInstant(zone);
		String date = startZoned.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
		String time = startZoned.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
		int duration = (int) java.time.Duration.between(startZoned.toOffsetDateTime(), endZoned.toOffsetDateTime()).toMinutes();
		String substitutedFor = b.getActivity().getSubstitutedFor() != null
				? (b.getActivity().getSubstitutedFor().getFirstName() + " " + b.getActivity().getSubstitutedFor().getLastName())
				: null;

		return new MyBookingDto(
			b.getId(),
			b.getActivity().getType() != null ? b.getActivity().getType().getActivityName() : "Zajęcia",
			b.getActivity().getInstructor() != null ? (b.getActivity().getInstructor().getFirstName() + " " + b.getActivity().getInstructor().getLastName()) : "Instruktor",
			date,
			time,
			duration,
			b.getActivity().getRoom().getName(),
			substitutedFor,
			substitutedFor != null
			);
	}
}