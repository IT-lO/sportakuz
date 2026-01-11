package com.icio.sportakuz.controller;

import com.icio.sportakuz.entity.*;
import com.icio.sportakuz.repo.*;
import com.icio.sportakuz.dto.ActivitySeriesForm;
import com.icio.sportakuz.repo.ClassStatus;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Kontroler CRUD serii zajęć (ActivitySeries) + generowanie wystąpień (Activity).
 */
@Controller
@RequestMapping("/activity-series")
public class ActivitySeriesController {
    private static final Logger log = LoggerFactory.getLogger(ActivitySeriesController.class);

    private final ActivitySeriesRepository activitySeriesRepository;
    private final ActivityRepository activityRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public ActivitySeriesController(ActivitySeriesRepository activitySeriesRepository,
                                    ActivityRepository activityRepository,
                                    ActivityTypeRepository activityTypeRepository,
                                    RoomRepository roomRepository,
                                    UserRepository userRepository) {
        this.activitySeriesRepository = activitySeriesRepository;
        this.activityRepository = activityRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    /** Lista serii */
    @GetMapping
    public String list(Model model){
        var all = activitySeriesRepository.findAll();
        List<ActivitySeries> active = new ArrayList<>();
        List<ActivitySeries> inactive = new ArrayList<>();

        for(var s : all){
            if(s.isActive()) active.add(s); else inactive.add(s);
        }

        // Sortowanie
        active.sort(Comparator.comparing(ActivitySeries::getStartTime));
        inactive.sort(Comparator.comparing(ActivitySeries::getRecurrenceUntil).reversed());

        model.addAttribute("activeSeries", active);
        model.addAttribute("inactiveSeries", inactive);
        return "activities/series_list";
    }

    /** Formularz nowej serii */
    @GetMapping("/new")
    public String newForm(Model model){
        ActivitySeriesForm f = new ActivitySeriesForm();
        // Domyślne wartości
        f.setDurationMinutes(60);
        f.setActive(true);

        model.addAttribute("form", f);
        addLookups(model);
        return "activities/new_recurring";
    }

    /** Tworzenie nowej serii */
    @PostMapping
    public String create(@Valid @ModelAttribute("form") ActivitySeriesForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra){
        validateDates(form, binding);
        if(binding.hasErrors()){
            addLookups(model);
            return "activities/new_recurring";
        }

        ActivitySeries s = new ActivitySeries();
        mapToEntity(form, s);

        activitySeriesRepository.save(s);
        generateOccurrencesForSeries(s);

        ra.addFlashAttribute("success", "Seria utworzona i wygenerowano pierwsze zajęcia.");
        return "redirect:/activity-series";
    }

    /** Formularz edycji serii */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra){
        var s = activitySeriesRepository.findById(id).orElse(null);
        if(s == null){
            ra.addFlashAttribute("error", "Seria nie znaleziona.");
            return "redirect:/activity-series";
        }
        model.addAttribute("form", toForm(s));
        model.addAttribute("editId", id);
        addLookups(model);
        return "activities/edit_recurring";
    }

    /** Aktualizacja serii - KLUCZOWA LOGIKA */
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("form") ActivitySeriesForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra){
        var s = activitySeriesRepository.findById(id).orElse(null);
        if(s == null){
            ra.addFlashAttribute("error", "Seria nie znaleziona.");
            return "redirect:/activity-series";
        }

        ZoneId zone = ZoneId.of("Europe/Warsaw");

        // Zapamiętujemy stary stan do obliczeń przesunięć
        LocalDate oldSeriesDate = s.getStartTime().atZoneSameInstant(zone).toLocalDate();
        RecurrencePattern oldPattern = s.getRecurrencePattern();

        validateDates(form, binding);
        if(binding.hasErrors()){
            addLookups(model);
            model.addAttribute("editId", id);
            return "activities/edit_recurring";
        }

        boolean patternChanged = oldPattern != form.getRecurrencePattern();

        try {
            // 1. Aktualizacja danych samej serii
            mapToEntity(form, s);
            activitySeriesRepository.save(s);

            OffsetDateTime now = OffsetDateTime.now(zone);
            var occurrences = activityRepository.findBySeries_Id(id); // Pobierz wszystkie należące do serii

            int updated = 0;
            int removed = 0;

            if(patternChanged){
                // SCENARIUSZ A: Zmienił się wzorzec (np. z Tygodniowo na Dziennie)
                // Usuwamy wszystkie PRZYSZŁE wystąpienia i generujemy nowe od nowa.
                for(var oc : occurrences){
                    // Pomiń przeszłe i zakończone
                    if(oc.getStartTime().isBefore(now)) continue;
                    if(oc.getStatus() == ClassStatus.CANCELLED || oc.getStatus() == ClassStatus.FINISHED) continue;

                    activityRepository.delete(oc);
                    removed++;
                }
                generateOccurrencesForSeries(s);
                ra.addFlashAttribute("success", "Zmieniono wzorzec powtarzania. Przeliczono kalendarz (usunięto: " + removed + ").");

            } else {
                // SCENARIUSZ B: Wzorzec ten sam (np. zmiana sali, instruktora lub godziny startu)
                // Aktualizujemy istniejące przyszłe rekordy
                LocalDate newSeriesDate = s.getStartTime().atZoneSameInstant(zone).toLocalDate();
                LocalTime newSeriesTime = s.getStartTime().atZoneSameInstant(zone).toLocalTime();

                for(var oc : occurrences){
                    // Pomiń przeszłe i anulowane
                    if(oc.getStartTime().isBefore(now)) continue;
                    if(oc.getStatus() == ClassStatus.CANCELLED || oc.getStatus() == ClassStatus.FINISHED) continue;

                    LocalDate occOldDate = oc.getStartTime().atZoneSameInstant(zone).toLocalDate();

                    // Obliczamy nową datę na podstawie przesunięcia w serii
                    LocalDate newDate;
                    switch (s.getRecurrencePattern()){
                        case DAILY -> {
                            long daysIndex = java.time.temporal.ChronoUnit.DAYS.between(oldSeriesDate, occOldDate);
                            newDate = newSeriesDate.plusDays(daysIndex);
                        }
                        case WEEKLY -> {
                            long weeksIndex = java.time.temporal.ChronoUnit.WEEKS.between(oldSeriesDate, occOldDate);
                            newDate = newSeriesDate.plusWeeks(weeksIndex);
                        }
                        case MONTHLY -> {
                            long monthsIndex = java.time.temporal.ChronoUnit.MONTHS.between(oldSeriesDate.withDayOfMonth(1), occOldDate.withDayOfMonth(1));
                            newDate = newSeriesDate.plusMonths(monthsIndex);
                        }
                        default -> newDate = newSeriesDate;
                    }

                    // Składamy nową datę i nową godzinę
                    LocalDateTime newStartLocal = LocalDateTime.of(newDate, newSeriesTime);
                    OffsetDateTime newStart = newStartLocal.atZone(zone).toOffsetDateTime();

                    // Aktualizacja pól Activity
                    oc.setStartTime(newStart);
                    oc.setDurationMinutes(s.getDurationMinutes()); // Nowy czas trwania
                    // endTime wyliczy się samo w @PreUpdate encji Activity!

                    oc.setType(s.getType());
                    oc.setInstructor(s.getInstructor());
                    oc.setRoom(s.getRoom());
                    oc.setCapacity(s.getCapacity());
                    oc.setNote(s.getNote());

                    activityRepository.save(oc);
                    updated++;
                }

                // Sprzątanie: Usuń wystąpienia które po zmianie daty wypadły poza 'recurrenceUntil'
                for(var oc : occurrences){
                    if(oc.getStartTime().isAfter(s.getRecurrenceUntil()) && oc.getStartTime().isAfter(now)){
                        // Sprawdź czy id nadal istnieje (mogło być usunięte w pętli wyżej, choć w tym flow nie powinno)
                        if(activityRepository.existsById(oc.getId())){
                            activityRepository.delete(oc);
                            removed++;
                        }
                    }
                }

                // Dotwórz, jeśli zakres się wydłużył
                generateOccurrencesForSeries(s);

                ra.addFlashAttribute("success", "Zaktualizowano serię (zmieniono: " + updated + ", usunięto: " + removed + ").");
            }
            return "redirect:/activity-series";

        } catch (Exception ex){
            log.error("[SERIES][{}] Błąd edycji: {}", id, ex.getMessage(), ex);
            ra.addFlashAttribute("error", "Błąd aktualizacji: " + ex.getMessage());
            return "redirect:/activity-series";
        }
    }

    /** Usunięcie serii wraz z wystąpieniami w statusie PLANNED; pozostałe odłączane od serii */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra){
        var s = activitySeriesRepository.findById(id).orElse(null);
        if (s == null) {
            ra.addFlashAttribute("error", "Seria nie znaleziona.");
            return "redirect:/activity-series";
        }

        var occurrences = activityRepository.findBySeries_Id(id);
        int deletedOccurrences = 0;
        int detachedOccurrences = 0;

        for (var oc : occurrences) {
            if (oc.getStatus() == ClassStatus.PLANNED) {
                // Usuń zaplanowane wystąpienia
                activityRepository.delete(oc);
                deletedOccurrences++;
            } else {
                // Pozostałe wystąpienia zostają w historii, ale bez powiązania z serią
                oc.setSeries(null);
                activityRepository.save(oc);
                detachedOccurrences++;
            }
        }

        activitySeriesRepository.delete(s);

        String msg = "Usunięto serię oraz " + deletedOccurrences + " zaplanowanych wystąpień.";
        if (detachedOccurrences > 0) {
            msg += " Odłączono " + detachedOccurrences + " istniejących wystąpień od serii.";
        }
        ra.addFlashAttribute("success", msg);
        return "redirect:/activity-series";
    }

    /** Toggle Active */
    @PostMapping("/{id}/active")
    public String toggleActive(@PathVariable("id") Long id, @RequestParam("active") boolean active, RedirectAttributes ra){
        var s = activitySeriesRepository.findById(id).orElse(null);
        if(s != null){
            s.setActive(active);
            activitySeriesRepository.save(s);
            if(active) generateOccurrencesForSeries(s); // Jeśli włączamy, od razu generuj
        }
        return "redirect:/activity-series";
    }

    /** Wymuszenie generowania */
    @PostMapping("/{id}/generate")
    public String manualGenerate(@PathVariable("id") Long id, RedirectAttributes ra){
        var s = activitySeriesRepository.findById(id).orElse(null);
        if(s != null){
            generateOccurrencesForSeries(s);
            ra.addFlashAttribute("success", "Wymuszono generowanie wystąpień.");
        }
        return "redirect:/activity-series";
    }

    // --- Helpers ---

    private void validateDates(ActivitySeriesForm form, BindingResult binding){
        if(form.getStartDate() != null && form.getUntilDate() != null){
            if(form.getUntilDate().isBefore(form.getStartDate())){
                binding.rejectValue("untilDate","range","Data końcowa nie może być przed datą startu");
            }
        }
    }

    private void mapToEntity(ActivitySeriesForm form, ActivitySeries s){
        s.setType(activityTypeRepository.findById(form.getActivityTypeId()).orElseThrow());
        s.setInstructor(userRepository.findById(form.getInstructorId()).orElseThrow());
        s.setRoom(roomRepository.findById(form.getRoomId()).orElseThrow());

        ZoneId zone = ZoneId.of("Europe/Warsaw");
        OffsetDateTime start = LocalDateTime.of(form.getStartDate(), form.getStartTime()).atZone(zone).toOffsetDateTime();

        s.setStartTime(start);
        s.setDurationMinutes(form.getDurationMinutes()); // Teraz używamy minut, nie endTime

        // Recurrence until = koniec dnia
        OffsetDateTime until = form.getUntilDate().atTime(23,59,59).atZone(zone).toOffsetDateTime();
        s.setRecurrenceUntil(until);

        s.setRecurrencePattern(form.getRecurrencePattern());
        s.setCapacity(form.getCapacity());
        s.setNote(form.getNote());
        s.setActive(form.isActive());
    }

    private ActivitySeriesForm toForm(ActivitySeries s){
        ActivitySeriesForm f = new ActivitySeriesForm();
        f.setActivityTypeId(s.getType().getId());
        f.setInstructorId(s.getInstructor().getId());
        f.setRoomId(s.getRoom().getId());

        ZoneId zone = ZoneId.of("Europe/Warsaw");
        f.setStartDate(s.getStartTime().atZoneSameInstant(zone).toLocalDate());
        f.setStartTime(s.getStartTime().atZoneSameInstant(zone).toLocalTime());

        f.setDurationMinutes(s.getDurationMinutes()); // Proste pobranie

        f.setUntilDate(s.getRecurrenceUntil().atZoneSameInstant(zone).toLocalDate());
        f.setRecurrencePattern(s.getRecurrencePattern());
        f.setCapacity(s.getCapacity());
        f.setNote(s.getNote());
        f.setActive(s.isActive());
        return f;
    }

    private void addLookups(Model model){
        model.addAttribute("types", activityTypeRepository.findAll());
        // tylko instruktorzy, nie wszyscy użytkownicy
        model.addAttribute("instructors", userRepository.findByRole(UserRole.ROLE_INSTRUCTOR));
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("patterns", RecurrencePattern.values());
    }

    /** * Generuje wystąpienia.
     * Logika: "Utwórz wszystko do 30 dni w przód LUB do recurrenceUntil (co nastąpi szybciej)".
     */
    private void generateOccurrencesForSeries(ActivitySeries s){
        if(!s.isActive()) return;

        ZoneId zone = ZoneId.of("Europe/Warsaw");
        OffsetDateTime now = OffsetDateTime.now(zone);
        OffsetDateTime horizon = now.plusDays(30);

        // Granica generowania: najwcześniejsza z (recurrenceUntil, now + 30 dni)
        OffsetDateTime limit = s.getRecurrenceUntil().isBefore(horizon) ? s.getRecurrenceUntil() : horizon;

        // Startujemy od daty z definicji serii
        OffsetDateTime cursor = s.getStartTime();

        // Jeśli seria zaczęła się w przeszłości, przesuń kursor na "dzisiaj" lub pierwsze przyszłe wystąpienie
        // (opcjonalne, zależnie czy chcesz uzupełniać historię, tu zakładam proste sprawdzenie exists)

        while(!cursor.isAfter(limit)){
            // Sprawdzamy czy już istnieje wystąpienie tej serii o tym czasie
            if(!activityRepository.existsBySeries_IdAndStartTime(s.getId(), cursor)){
                // Jeśli data jest w przeszłości (przed "teraz" minus np. 1h), można pominąć generowanie
                // żeby nie śmiecić kalendarza wstecz, jeśli edytujemy starą serię.
                // Tu zostawiam logikę: generuj wszystko co w zakresie.

                Activity oc = new Activity();
                oc.setSeries(s);
                oc.setType(s.getType());
                oc.setInstructor(s.getInstructor());
                oc.setRoom(s.getRoom());

                oc.setStartTime(cursor);
                oc.setDurationMinutes(s.getDurationMinutes()); // Ustawiamy trwanie
                // endTime wyliczy się samo

                oc.setCapacity(s.getCapacity());
                oc.setStatus(ClassStatus.PLANNED);
                oc.setNote(s.getNote());

                activityRepository.save(oc);
            }
            cursor = next(cursor, s.getRecurrencePattern());
        }
    }

    private OffsetDateTime next(OffsetDateTime current, RecurrencePattern pattern){
        return switch (pattern){
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
        };
    }
}
