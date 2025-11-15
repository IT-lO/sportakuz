package com.icio.sportakuz.calendar.web;

import com.icio.sportakuz.classes.domain.ClassOccurrence;
import com.icio.sportakuz.classes.repo.BookingRepository;
import com.icio.sportakuz.classes.repo.ClassOccurrenceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kontroler widoku kalendarza. Ładuje wystąpienia zajęć z bazy i udostępnia je
 * jako uproszczone DTO serializowane przez Thymeleaf do JS.
 */
@Controller
@RequestMapping("/calendar")
public class CalendarController {

    private final ClassOccurrenceRepository classOccurrenceRepository;
    private final BookingRepository bookingRepository;
    private final ZoneId zone = ZoneId.of("Europe/Warsaw");

    public CalendarController(ClassOccurrenceRepository classOccurrenceRepository,
                              BookingRepository bookingRepository) {
        this.classOccurrenceRepository = classOccurrenceRepository;
        this.bookingRepository = bookingRepository;
    }

    /** GET /calendar – główny widok kalendarza. */
    @GetMapping
    public String calendarRoot(Model model) {
        model.addAttribute("pageTitle", "Kalendarz Zajęć Sportowych");
        // Pobieramy tylko przyszłe nieanulowane zajęcia (widoczne w kalendarzu)
        List<ClassOccurrence> occurrences = classOccurrenceRepository.findNextVisible(OffsetDateTime.now(), org.springframework.data.domain.Pageable.unpaged());
        List<CalendarClassDto> dtoList = occurrences.stream().map(this::toDto).collect(Collectors.toList());
        model.addAttribute("classes", dtoList);
        return "calendar/calendar";
    }

    private CalendarClassDto toDto(ClassOccurrence c) {
        var startZoned = c.getStartTime().atZoneSameInstant(zone);
        var endZoned = c.getEndTime().atZoneSameInstant(zone);
        int dayIndex = startZoned.getDayOfWeek().getValue() - 1; // Monday->0
        String date = startZoned.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
        String time = startZoned.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        int duration = (int) java.time.Duration.between(startZoned.toOffsetDateTime(), endZoned.toOffsetDateTime()).toMinutes();
        long reserved = bookingRepository.countActiveByClassId(c.getId());
        String spots = reserved + "/" + c.getCapacity();
        String level = c.getType() != null ? c.getType().getDifficulty() : null;
        return new CalendarClassDto(
            c.getId(),
            c.getType() != null ? c.getType().getName() : "Zajęcia",
            dayIndex,
            date,
            time,
            duration,
            c.getRoom() != null ? c.getRoom().getName() : "Sala",
            c.getInstructor() != null ? (c.getInstructor().getFirstName() + " " + c.getInstructor().getLastName()) : "Instruktor",
            spots,
            level == null ? "" : level
        );
    }

    /**
     * Uproszczony DTO przekazywany do JS (lista).
     * Pola:
     *  id          - identyfikator wystąpienia
     *  name        - nazwa typu zajęć
     *  day         - indeks dnia tygodnia (0=Pon)
     *  date        - pełna data (yyyy-MM-dd) do filtrowania tygodni
     *  time        - godzina startu (HH:mm)
     *  duration    - czas trwania w minutach
    *  room        - nazwa sali
    *  instructor  - imię i nazwisko instruktora
     *  spots       - zajęte/pojemność
     *  level       - trudność (lub pusty string)
     */
    public record CalendarClassDto(Long id,
                                   String name,
                                   int day,
                                   String date,
                                   String time,
                                   int duration,
                                   String room,
                                   String instructor,
                                   String spots,
                                   String level) {}
}
