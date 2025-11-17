package com.icio.sportakuz.bookings.controllers;

import com.icio.sportakuz.classes.domain.Booking;
import com.icio.sportakuz.classes.domain.BookingStatus;
import com.icio.sportakuz.classes.domain.ClassOccurrence;
import com.icio.sportakuz.classes.repo.BookingRepository;
import com.icio.sportakuz.classes.repo.ClassOccurrenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API do tworzenia rezerwacji z widoku kalendarza.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    private final BookingRepository bookingRepository;
    private final ClassOccurrenceRepository occurrenceRepository;

    public BookingApiController(BookingRepository bookingRepository,
                                ClassOccurrenceRepository occurrenceRepository) {
        this.bookingRepository = bookingRepository;
        this.occurrenceRepository = occurrenceRepository;
    }

    /**
     * Tworzy nową rezerwację w statusie REQUESTED dla podanego wystąpienia zajęć.
     * Walidacje: istnieje klasa, nie jest CANCELLED, nie przekroczono pojemności, brak duplikatu userName.
     */
    @Transactional
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateBookingRequest req) {
        if (req.classId() == null || req.userName() == null || req.userName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Brak wymaganych danych"));
        }
        ClassOccurrence occurrence = occurrenceRepository.findByIdForUpdate(req.classId());
        if (occurrence == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Nie znaleziono zajęć"));
        }
        if (occurrence.getStatus() == com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Zajęcia anulowane"));
        }
        long reserved = bookingRepository.countActiveByClassId(occurrence.getId());
        if (reserved >= occurrence.getCapacity()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Brak wolnych miejsc"));
        }
        // Sprawdź duplikat aktywnej rezerwacji użytkownika
        boolean already = bookingRepository.existsByClazz_IdAndUserNameAndStatusIn(
                occurrence.getId(), req.userName(), List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED, BookingStatus.PAID));
        if (already) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Masz już aktywną rezerwację"));
        }

        Booking booking = new Booking();
        booking.setClazz(occurrence);
        booking.setUserName(req.userName().trim());
        booking.setStatus(BookingStatus.REQUESTED);
        bookingRepository.save(booking);

        long newReserved = bookingRepository.countActiveByClassId(occurrence.getId());
        String spots = newReserved + "/" + occurrence.getCapacity();
        return ResponseEntity.ok(new BookingResponse(booking.getId(), spots));
    }

    /**
     * Usuwa rezerwację na podstawie jej ID.
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody CancelBookingRequest req) {
        if (req.bookingId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Brak wymaganych danych"));
        }

        Booking booking = bookingRepository.findById(req.bookingId()).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Nie znaleziono rezerwacji do usunięcia"));
        }
        bookingRepository.delete(booking);
        return ResponseEntity.ok(new CancelBookingResponse());
    }

    /**
     * Usuwa rezerwację na podstawie ID klasy oraz nazwy użytkownika.
     */
    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody DeleteBookingRequest req) {
        if (req.classId() == null || req.userName() == null || req.userName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Brak wymaganych danych"));
        }
        ClassOccurrence occurrence = occurrenceRepository.findById(req.classId()).orElse(null);
        if (occurrence == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Nie znaleziono zajęć"));
        }

        // Pobierz rezerwację użytkownika do usunięcia
        Booking booking = bookingRepository.findFirstByClazz_IdAndUserNameAndStatusIn(
                occurrence.getId(), req.userName(), List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED, BookingStatus.PAID));
        if (booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Nie znaleziono rezerwacji do usunięcia"));
        }
        bookingRepository.delete(booking);

        long newReserved = bookingRepository.countActiveByClassId(occurrence.getId());
        String spots = newReserved + "/" + occurrence.getCapacity();
        return ResponseEntity.ok(new BookingResponse(booking.getId(), spots));
    }

    public record CancelBookingRequest(Long bookingId) {}
    public record CancelBookingResponse() {}
    public record DeleteBookingRequest(Long classId, String userName) {}
    public record CreateBookingRequest(Long classId, String userName) {}
    public record BookingResponse(Long id, String spots) {}
    public record ErrorResponse(String error) {}
}
