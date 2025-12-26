package com.icio.sportakuz.controller;

import com.icio.sportakuz.dto.ClassOccurrenceForm;
import com.icio.sportakuz.entity.ActivityType;
import com.icio.sportakuz.entity.Activity;
import com.icio.sportakuz.entity.Room;
import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.repo.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kontroler CRUD dla pojedynczych wystąpień zajęć (ClassOccurrence).
 * Odpowiada za:
 *  - listowanie wszystkich wystąpień (proste bez paginacji – do rozbudowy),
 *  - wyświetlenie formularza tworzenia / edycji,
 *  - walidację Bean Validation oraz kolizji (sala / instruktor) przed zapisem,
 *  - utworzenie, aktualizację oraz usuwanie wystąpień.
 * Konwersja pomiędzy formularzem a encją uwzględnia strefę czasu (Europe/Warsaw).
 */
@Controller
@RequestMapping("/activities")
@Slf4j
public class ClassOccurrenceController {

    private final ActivityRepository activityRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public ClassOccurrenceController(ActivityRepository activityRepository,
                                     ActivityTypeRepository activityTypeRepository,
                                     UserRepository userRepository,
                                     RoomRepository roomRepository,
                                     BookingRepository bookingRepository) {
        this.activityRepository = activityRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    /** GET /activities – lista wystąpień (do rozbudowy np. o filtrowanie/paginację). */
    @GetMapping
    public String list(Model model, @RequestParam(value = "pattern", required = false) String pattern) {
        String likePattern = (pattern == null || pattern.isBlank()) ? null : (pattern.trim().toLowerCase() + "%");
        var all = (likePattern == null) ? activityRepository.findAllByOrderByStartTimeAsc() : activityRepository.searchOrderByStartTimeAsc(likePattern);
        java.util.List<Activity> upcoming = new java.util.ArrayList<>();
        java.util.List<Activity> history = new java.util.ArrayList<>();
        for (var oc : all) {
            if (oc.getStatus() == ClassStatus.CANCELLED || oc.getStatus() == ClassStatus.FINISHED) {
                history.add(oc);
            } else {
                upcoming.add(oc);
            }
        }
        // Historia – odwrotnie (najnowsze na górze) – sortujemy malejąco po starcie
        history.sort(java.util.Comparator.comparing(Activity::getStartTime).reversed());

        // Mapa dostępnych instruktorów – tylko dla upcoming
        var allInstructors = userRepository.findAll();
        Map<Long, List<User>> availableMap = new HashMap<>();
        Map<Long, Long> activeBookingsCount = new HashMap<>(); //  liczności rezerwacji
        for (var oc : upcoming) {
            java.util.List<User> avail = new java.util.ArrayList<>();
            for (var instr : allInstructors) {
                if (!instr.isActive()) continue;
                if (instr.getId().equals(oc.getInstructor().getId())) {
                    avail.add(instr); // obecny zawsze
                    continue;
                }
                var overlapping = activityRepository.findOverlappingForInstructor(instr.getId(), oc.getStartTime(), oc.getEndTime())
                        .stream().filter(c -> c.getStatus() != ClassStatus.CANCELLED).toList();
                if (overlapping.isEmpty()) {
                    avail.add(instr);
                }
            }
            availableMap.put(oc.getId(), avail);
            activeBookingsCount.put(oc.getId(), bookingRepository.countActiveByClassId(oc.getId())); // ile aktywnych rezerwacji
        }
        model.addAttribute("activities", upcoming); // główna lista = przyszłe
        model.addAttribute("historyActivities", history); // historia = anulowane / zakończone
        model.addAttribute("allStatuses", ClassStatus.values());
        model.addAttribute("instructors", allInstructors);
        model.addAttribute("availableInstructors", availableMap);
        model.addAttribute("activeBookings", activeBookingsCount);
        model.addAttribute("pattern", pattern);
        return "activities/list";
    }

    /** GET /activities/new – formularz tworzenia nowego wystąpienia z domyślnym czasem trwania. */
    @GetMapping("/new")
    public String createForm(Model model) {
        ClassOccurrenceForm f = new ClassOccurrenceForm();
        // Nie ustawiamy tutaj domyślnego czasu – zostanie wstawiony przez JS po wyborze typu.
        model.addAttribute("form", f);
        addLookups(model);
        return "activities/new";
    }

    /** Wylicza OffsetDateTime startu na podstawie daty + lokalnego czasu. */
    private OffsetDateTime computeStart(ClassOccurrenceForm form) {
        if (form.getDate() == null || form.getStartTime() == null) return null;
        var zone = ZoneId.of("Europe/Warsaw");
        return LocalDateTime.of(form.getDate(), form.getStartTime()).atZone(zone).toOffsetDateTime();
    }

    /** Dodaje minuty do startu zwracając czas zakończenia; zwraca null jeśli brak danych lub niepoprawne wartości. */
    private OffsetDateTime computeEnd(OffsetDateTime start, Integer durationMinutes) {
        if (start == null || durationMinutes == null || durationMinutes <= 0) return null;
        return start.plusMinutes(durationMinutes);
    }

    /**
     * Waliduje kolizje sali/instruktora w podanym przedziale czasowym.
     * Jeśli editingId != null – ignoruje kolizję z własnym wystąpieniem podczas edycji.
     */
    private void validateConflicts(ClassOccurrenceForm form, BindingResult binding, Long editingId) {
        if (binding.hasErrors()) return; // wcześniejsze błędy
        var start = computeStart(form);
        var end = computeEnd(start, form.getDurationMinutes());
        if (start == null || end == null) return; // brak danych czasowych

        // Sprawdzenie kolizji sali – NIE zależy od wybranego instruktora
        if (form.getRoomId() != null) {
            long roomCnt = activityRepository.countOverlappingInRoom(form.getRoomId(), start, end);
            if (editingId != null && roomCnt > 0) {
                roomCnt = activityRepository.findOverlappingInRoom(form.getRoomId(), start, end)
                        .stream().filter(c -> !c.getId().equals(editingId)).count();
            }
            if (roomCnt > 0) {
                log.debug("[CONFLICT][ROOM] roomId={} start={} end={} count={}", form.getRoomId(), start, end, roomCnt);
                binding.rejectValue("roomId", "conflict.room", "Sala zajęta w tym czasie");
            }
        }
        // Sprawdzenie kolizji instruktora – tylko jeśli wybrany instruktor
        if (form.getInstructorId() != null) {
            long instrCnt = activityRepository.countOverlappingForInstructor(form.getInstructorId(), start, end);
            if (editingId != null && instrCnt > 0) {
                instrCnt = activityRepository.findOverlappingForInstructor(form.getInstructorId(), start, end)
                        .stream().filter(c -> !c.getId().equals(editingId)).count();
            }
            if (instrCnt > 0) {
                log.debug("[CONFLICT][INSTR] instructorId={} start={} end={} count={}", form.getInstructorId(), start, end, instrCnt);
                binding.rejectValue("instructorId", "conflict.instructor", "Instruktor prowadzi zajęcia w tym czasie");
            }
        }
    }

    /** POST /activities – tworzy nowe wystąpienie po walidacji formularza i kolizji. */
    @PostMapping
    public String create(@Valid @ModelAttribute("form") ClassOccurrenceForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra) {

        Room room = null;
        if (form.getRoomId() != null) {
            room = roomRepository.findById(form.getRoomId()).orElse(null);
        }
        // Pobierz typ zajęć aby użyć jego domyślnego czasu jeśli formularz nie podał poprawnego
        ActivityType activityType = null;
        if (form.getActivityTypeId() != null) {
            activityType = activityTypeRepository.findById(form.getActivityTypeId()).orElse(null);
        }
        if (form.getDurationMinutes() == null || form.getDurationMinutes() <= 0) {
            if (activityType != null && activityType.getDuration() != null && activityType.getDuration() > 0) {
                form.setDurationMinutes(activityType.getDuration());
            } else {
                form.setDurationMinutes(60); // ogólny fallback
            }
        }
        if (form.getDurationMinutes() != null && form.getDurationMinutes() > 10000) {
            binding.rejectValue("durationMinutes", "duration.tooLarge", "Czas trwania zbyt duży");
        }
        if ((form.getCapacity() == null || form.getCapacity() <= 0) && room != null) {
            form.setCapacity(room.getCapacity());
        }
        if (room != null && form.getCapacity() != null && form.getCapacity() > room.getCapacity()) {
            form.setCapacity(room.getCapacity());
        }
        // walidacja kolizji
        validateConflicts(form, binding, null);
        if (binding.hasErrors()) {
            addLookups(model);
            return "activities/new";
        }

        Activity oc = new Activity();
        oc.setType(activityTypeRepository.findById(form.getActivityTypeId()).orElseThrow());
        oc.setInstructor(userRepository.findById(form.getInstructorId()).orElseThrow());
        oc.setRoom(roomRepository.findById(form.getRoomId()).orElseThrow());

        var zone = ZoneId.of("Europe/Warsaw");
        var start = LocalDateTime.of(form.getDate(), form.getStartTime()).atZone(zone).toOffsetDateTime();
        oc.setStartTime(start);
        oc.setDurationMinutes(form.getDurationMinutes());

        oc.setCapacity(form.getCapacity());
        oc.setStatus(ClassStatus.PLANNED); // na start
        oc.setNote(form.getNote());

        activityRepository.save(oc);

        ra.addFlashAttribute("success", "Zajęcia dodane.");
        return "redirect:/activities";
    }

    /** POST /activities/{id}/delete – usuwa wystąpienie jeśli brak aktywnych rezerwacji; inaczej blokuje. */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        var oc = activityRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("error", "Zajęcia nie zostały znalezione (id=" + id + ").");
            return "redirect:/activities";
        }
        // Sprawdza, czy są jakieś aktywne rezerwacje (Requested/Confirmed/Paid)
        long activeBookings = bookingRepository.countActiveByClassId(id);
        if (activeBookings > 0) {
            ra.addFlashAttribute("error", "Nie można usunąć zajęć " + occurrenceLabel(oc) + ", istnieją aktywne rezerwacje (" + activeBookings + ").");
            return "redirect:/activities";
        }
        activityRepository.deleteById(id);
        ra.addFlashAttribute("success", "Zajęcia " + occurrenceLabel(oc) + " usunięte.");
        return "redirect:/activities";
    }

    /** GET /activities/{id}/edit – formularz edycji istniejącego wystąpienia lub redirect jeśli brak. */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        var oc = activityRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("success", "Zajęcia nie znalezione (id=" + id + ").");
            return "redirect:/activities";
        }
        ClassOccurrenceForm form = toForm(oc);
        model.addAttribute("form", form);
        model.addAttribute("editId", id);
        addLookups(model);
        return "activities/edit";
    }

    /** POST /activities/{id} – aktualizacja istniejącego wystąpienia po walidacji. */
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("form") ClassOccurrenceForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra) {
        var oc = activityRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("success", "Zajęcia nie znalezione (id=" + id + ").");
            return "redirect:/activities";
        }
        Room room = null;
        if (form.getRoomId() != null) {
            room = roomRepository.findById(form.getRoomId()).orElse(null);
        }
        ActivityType activityType = null;
        if (form.getActivityTypeId() != null) {
            activityType = activityTypeRepository.findById(form.getActivityTypeId()).orElse(null);
        }
        if (form.getDurationMinutes() == null || form.getDurationMinutes() <= 0) {
            if (activityType != null && activityType.getDuration() != null && activityType.getDuration() > 0) {
                form.setDurationMinutes(activityType.getDuration());
            } else {
                form.setDurationMinutes(60);
            }
        }
        if (form.getDurationMinutes() != null && form.getDurationMinutes() > 10000) {
            binding.rejectValue("durationMinutes", "duration.tooLarge", "Czas trwania zbyt duży");
        }
        if ((form.getCapacity() == null || form.getCapacity() <= 0) && room != null) {
            form.setCapacity(room.getCapacity());
        }
        if (room != null && form.getCapacity() != null && form.getCapacity() > room.getCapacity()) {
            form.setCapacity(room.getCapacity());
        }
        // walidacja kolizji (ignorując bieżące id)
        validateConflicts(form, binding, id);
        if (binding.hasErrors()) {
            addLookups(model);
            model.addAttribute("editId", id);
            return "activities/edit";
        }

        oc.setType(activityTypeRepository.findById(form.getActivityTypeId()).orElseThrow());
        oc.setInstructor(userRepository.findById(form.getInstructorId()).orElseThrow());
        oc.setRoom(roomRepository.findById(form.getRoomId()).orElseThrow());

        var zone = ZoneId.of("Europe/Warsaw");
        OffsetDateTime start = LocalDateTime.of(form.getDate(), form.getStartTime()).atZone(zone).toOffsetDateTime();
        OffsetDateTime end = start.plusMinutes(form.getDurationMinutes());
        oc.setStartTime(start);
        oc.setEndTime(end);
        oc.setCapacity(form.getCapacity());
        oc.setNote(form.getNote());

        activityRepository.save(oc);
        ra.addFlashAttribute("success", "Zajęcia " + occurrenceLabel(oc) + " zaktualizowane.");
        return "redirect:/activities";
    }

    /** Mapuje encję na formularz (rekonstruuje datę, lokalny czas i duration z różnicy czasów). */
    private ClassOccurrenceForm toForm(Activity oc) {
        ClassOccurrenceForm f = new ClassOccurrenceForm();
        f.setActivityTypeId(oc.getType().getId());
        f.setInstructorId(oc.getInstructor().getId());
        f.setRoomId(oc.getRoom().getId());
        var start = oc.getStartTime();
        var end = oc.getEndTime();
        var zone = ZoneId.of("Europe/Warsaw");
        f.setDate(start.atZoneSameInstant(zone).toLocalDate());
        f.setStartTime(start.atZoneSameInstant(zone).toLocalTime());
        // wyliczamy duration z różnicy czasu
        long durationMinutes = java.time.Duration.between(start, end).toMinutes();
        f.setDurationMinutes((int) durationMinutes);
        f.setCapacity(oc.getCapacity());
        f.setNote(oc.getNote());
        return f;
    }

    /** Dodaje listy typów, instruktorów i sal do modelu dla formularzy. */
    private void addLookups(Model model) {
        model.addAttribute("types", activityTypeRepository.findAll());
        model.addAttribute("instructors", userRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
    }

    /** Sprawdza czy przejście statusu jest dozwolone (prosta maszyna stanów). */
    private boolean isAllowedTransition(ClassStatus current, ClassStatus target) {
        if (current == null || target == null) return false;
        if (current == target) return true; // pozostawienie bez zmian
        return switch (current) {
            case PLANNED -> (target == ClassStatus.OPEN || target == ClassStatus.CANCELLED);
            case OPEN -> (target == ClassStatus.FINISHED || target == ClassStatus.CANCELLED);
            case CANCELLED, FINISHED -> false; // stan końcowy / archiwalny
        };
    }

    /** POST /activities/{id}/status – zmiana statusu pojedynczego wystąpienia zajęć. */
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") ClassStatus newStatus,
                               RedirectAttributes ra) {
        var oc = activityRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("error", "Zajęcia nie znalezione (id=" + id + ").");
            return "redirect:/activities";
        }
        var current = oc.getStatus();
        if (!isAllowedTransition(current, newStatus)) {
            ra.addFlashAttribute("error", "Niepoprawna zmiana statusu z " + current.getLabel() + " na " + newStatus.getLabel() + ".");
            return "redirect:/activities";
        }
        oc.setStatus(newStatus);
        activityRepository.save(oc);
        ra.addFlashAttribute("success", "Status zajęć " + occurrenceLabel(oc) + " zmieniony na " + newStatus.getLabel() + ".");
        return "redirect:/activities";
    }

    /** POST /activities/{id}/instructor – zmiana instruktora (zastępstwo) dla pojedynczego wystąpienia. */
    @PostMapping("/{id}/instructor")
    public String updateInstructor(@PathVariable("id") Long id,
                                   @RequestParam("instructorId") Long instructorId,
                                   RedirectAttributes ra) {
        var oc = activityRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("error", "Zajęcia nie znalezione (id=" + id + ").");
            return "redirect:/activities";
        }
        if (instructorId == null) {
            ra.addFlashAttribute("error", "Brak identyfikatora instruktora.");
            return "redirect:/activities";
        }
        var newInstr = userRepository.findById(instructorId).orElse(null);
        if (newInstr == null) {
            ra.addFlashAttribute("error", "Instruktor nie znaleziony (id=" + instructorId + ").");
            return "redirect:/activities";
        }
        if (!newInstr.isActive()) {
            ra.addFlashAttribute("error", "Instruktor jest nieaktywny.");
            return "redirect:/activities";
        }
        // Jeśli wybieramy ponownie aktualnego instruktora – brak zmian
        if (oc.getInstructor().getId().equals(instructorId)) {
            ra.addFlashAttribute("success", "Instruktor bez zmian.");
            return "redirect:/activities";
        }
        // Jeśli mamy zastępstwo i wracamy do instruktora pierwotnego – przywróć i wyczyść substitutedFor
        if (oc.getSubstitutedFor() != null && oc.getSubstitutedFor().getId().equals(instructorId)) {
            oc.setInstructor(newInstr); // newInstr to oryginalny
            oc.setSubstitutedFor(null);
            activityRepository.save(oc);
            ra.addFlashAttribute("success", "Powrót do instruktora pierwotnego: " + newInstr.getFirstName() + " " + newInstr.getLastName() + ".");
            return "redirect:/activities";
        }
        // Walidacja kolizji czasowej dla nowego instruktora (ignorując bieżące wystąpienie i CANCELLED)
        var conflicts = activityRepository.findOverlappingForInstructor(instructorId, oc.getStartTime(), oc.getEndTime())
            .stream().filter(c -> !c.getId().equals(oc.getId()) && c.getStatus() != ClassStatus.CANCELLED).count();
        if (conflicts > 0) {
            ra.addFlashAttribute("error", "Instruktor ma kolizję w tym przedziale czasu.");
            return "redirect:/activities";
        }
        // Ustal pierwotnego instruktora: jeśli jeszcze nie było zastępstwa, zapisz obecnego jako substitutedFor; inaczej pozostaw istniejącego.
        if (oc.getSubstitutedFor() == null) {
            oc.setSubstitutedFor(oc.getInstructor());
        }
        oc.setInstructor(newInstr);
        activityRepository.save(oc);
        ra.addFlashAttribute("success", "Instruktor zajęć " + occurrenceLabel(oc) + " zmieniony na: " + newInstr.getFirstName() + " " + newInstr.getLastName() + ".");
        return "redirect:/activities";
    }

    /** Pomocnicza etykieta wystąpienia: [YYYY-MM-DD] Typ HH:mm-HH:mm - Instruktor (Zastępstwo za: Stary Instruktor) */
    private String occurrenceLabel(Activity oc) {
        if (oc == null) return "[nieznane]";
        try {
            ZoneId zone = ZoneId.of("Europe/Warsaw");
            LocalDateTime startLocal = oc.getStartTime().atZoneSameInstant(zone).toLocalDateTime();
            LocalDateTime endLocal = oc.getEndTime().atZoneSameInstant(zone).toLocalDateTime();
            DateTimeFormatter dateF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeF = DateTimeFormatter.ofPattern("HH:mm");
            String date = dateF.format(startLocal);
            String times = timeF.format(startLocal) + "-" + timeF.format(endLocal);
            String type = oc.getType() != null ? oc.getType().getActivityName() : "-";
            String instr = oc.getInstructor() != null ? (oc.getInstructor().getFirstName() + " " + oc.getInstructor().getLastName()) : "-";
            String subst = oc.getSubstitutedFor() != null ? " (Zastępstwo za: " + oc.getSubstitutedFor().getFirstName() + " " + oc.getSubstitutedFor().getLastName() + ")" : "";
            return "[" + date + "] "  + type + " " + times +  " - " + instr + subst;
        } catch (Exception ex) {
            return "[nieznane]";
        }
    }
}
