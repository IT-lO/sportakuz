package com.icio.sportakuz.controller;

import com.icio.sportakuz.dto.ClassOccurrenceForm;
import com.icio.sportakuz.entity.ClassOccurrence;
import com.icio.sportakuz.entity.ClassType;
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
@RequestMapping("/classes")
@Slf4j
public class ClassOccurrenceController {

    private final ClassOccurrenceRepository classOccurrenceRepository;
    private final ClassTypeRepository classTypeRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public ClassOccurrenceController(ClassOccurrenceRepository classOccurrenceRepository,
                                     ClassTypeRepository classTypeRepository,
                                     UserRepository userRepository,
                                     RoomRepository roomRepository,
                                     BookingRepository bookingRepository) {
        this.classOccurrenceRepository = classOccurrenceRepository;
        this.classTypeRepository = classTypeRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    /** GET /classes – lista wystąpień (do rozbudowy np. o filtrowanie/paginację). */
    @GetMapping
    public String list(Model model) {
        // Wszystkie wystąpienia posortowane po starcie
        var all = classOccurrenceRepository.findAllByOrderByStartTimeAsc();
        java.util.List<ClassOccurrence> upcoming = new java.util.ArrayList<>();
        java.util.List<ClassOccurrence> history = new java.util.ArrayList<>();
        for (var oc : all) {
            if (oc.getStatus() == ClassStatus.CANCELLED || oc.getStatus() == ClassStatus.FINISHED) {
                history.add(oc);
            } else {
                upcoming.add(oc);
            }
        }
        // Historia – odwrotnie (najnowsze na górze) – sortujemy malejąco po starcie
        history.sort(java.util.Comparator.comparing(ClassOccurrence::getStartTime).reversed());

        // Mapa dostępnych instruktorów – tylko dla upcoming
        var allInstructors = userRepository.findAll();
        java.util.Map<Long, java.util.List<User>> availableMap = new java.util.HashMap<>();
        java.util.Map<Long, Long> activeBookingsCount = new java.util.HashMap<>(); //  liczności rezerwacji
        for (var oc : upcoming) {
            java.util.List<User> avail = new java.util.ArrayList<>();
            for (var instr : allInstructors) {
                if (!instr.isActive()) continue;
                if (instr.getId().equals(oc.getInstructor().getId())) {
                    avail.add(instr); // obecny zawsze
                    continue;
                }
                var overlapping = classOccurrenceRepository.findOverlappingForInstructor(instr.getId(), oc.getStartTime(), oc.getEndTime())
                        .stream().filter(c -> c.getStatus() != ClassStatus.CANCELLED).toList();
                if (overlapping.isEmpty()) {
                    avail.add(instr);
                }
            }
            availableMap.put(oc.getId(), avail);
            activeBookingsCount.put(oc.getId(), bookingRepository.countActiveByClassId(oc.getId())); // ile aktywnych rezerwacji
        }
        model.addAttribute("classes", upcoming); // główna lista = przyszłe
        model.addAttribute("historyClasses", history); // historia = anulowane / zakończone
        model.addAttribute("allStatuses", ClassStatus.values());
        model.addAttribute("instructors", allInstructors);
        model.addAttribute("availableInstructors", availableMap);
        model.addAttribute("activeBookings", activeBookingsCount);
        return "classes/list";
    }

    /** GET /classes/new – formularz tworzenia nowego wystąpienia z domyślnym czasem trwania. */
    @GetMapping("/new")
    public String createForm(Model model) {
        ClassOccurrenceForm f = new ClassOccurrenceForm();
        // Nie ustawiamy tutaj domyślnego czasu – zostanie wstawiony przez JS po wyborze typu.
        model.addAttribute("form", f);
        addLookups(model);
        return "classes/new";
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
            long roomCnt = classOccurrenceRepository.countOverlappingInRoom(form.getRoomId(), start, end);
            if (editingId != null && roomCnt > 0) {
                roomCnt = classOccurrenceRepository.findOverlappingInRoom(form.getRoomId(), start, end)
                        .stream().filter(c -> !c.getId().equals(editingId)).count();
            }
            if (roomCnt > 0) {
                log.debug("[CONFLICT][ROOM] roomId={} start={} end={} count={}", form.getRoomId(), start, end, roomCnt);
                binding.rejectValue("roomId", "conflict.room", "Sala zajęta w tym czasie");
            }
        }
        // Sprawdzenie kolizji instruktora – tylko jeśli wybrany instruktor
        if (form.getInstructorId() != null) {
            long instrCnt = classOccurrenceRepository.countOverlappingForInstructor(form.getInstructorId(), start, end);
            if (editingId != null && instrCnt > 0) {
                instrCnt = classOccurrenceRepository.findOverlappingForInstructor(form.getInstructorId(), start, end)
                        .stream().filter(c -> !c.getId().equals(editingId)).count();
            }
            if (instrCnt > 0) {
                log.debug("[CONFLICT][INSTR] instructorId={} start={} end={} count={}", form.getInstructorId(), start, end, instrCnt);
                binding.rejectValue("instructorId", "conflict.instructor", "Instruktor prowadzi zajęcia w tym czasie");
            }
        }
    }

    /** POST /classes – tworzy nowe wystąpienie po walidacji formularza i kolizji. */
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
        ClassType classType = null;
        if (form.getClassTypeId() != null) {
            classType = classTypeRepository.findById(form.getClassTypeId()).orElse(null);
        }
        if (form.getDurationMinutes() == null || form.getDurationMinutes() <= 0) {
            if (classType != null && classType.getDefaultDurationMinutes() != null && classType.getDefaultDurationMinutes() > 0) {
                form.setDurationMinutes(classType.getDefaultDurationMinutes());
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
            return "classes/new";
        }

        ClassOccurrence oc = new ClassOccurrence();
        oc.setType(classTypeRepository.findById(form.getClassTypeId()).orElseThrow());
        oc.setInstructor(userRepository.findById(form.getInstructorId()).orElseThrow());
        oc.setRoom(roomRepository.findById(form.getRoomId()).orElseThrow());

        var zone = ZoneId.of("Europe/Warsaw");
        var start = LocalDateTime.of(form.getDate(), form.getStartTime()).atZone(zone).toOffsetDateTime();
        var end = start.plusMinutes(form.getDurationMinutes());
        oc.setStartTime(start);
        oc.setEndTime(end);

        oc.setCapacity(form.getCapacity());
        oc.setStatus(ClassStatus.PLANNED); // na start
        oc.setNote(form.getNote());

        classOccurrenceRepository.save(oc);

        ra.addFlashAttribute("success", "Zajęcia dodane.");
        return "redirect:/classes";
    }

    /** POST /classes/{id}/delete – usuwa wystąpienie jeśli brak aktywnych rezerwacji; inaczej blokuje. */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        var oc = classOccurrenceRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("error", "Zajęcia nie zostały znalezione (id=" + id + ").");
            return "redirect:/classes";
        }
        // Sprawdza, czy są jakieś aktywne rezerwacje (Requested/Confirmed/Paid)
        long activeBookings = bookingRepository.countActiveByClassId(id);
        if (activeBookings > 0) {
            ra.addFlashAttribute("error", "Nie można usunąć zajęć " + occurrenceLabel(oc) + ", istnieją aktywne rezerwacje (" + activeBookings + ").");
            return "redirect:/classes";
        }
        classOccurrenceRepository.deleteById(id);
        ra.addFlashAttribute("success", "Zajęcia " + occurrenceLabel(oc) + " usunięte.");
        return "redirect:/classes";
    }

    /** GET /classes/{id}/edit – formularz edycji istniejącego wystąpienia lub redirect jeśli brak. */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        var oc = classOccurrenceRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("success", "Zajęcia nie znalezione (id=" + id + ").");
            return "redirect:/classes";
        }
        ClassOccurrenceForm form = toForm(oc);
        model.addAttribute("form", form);
        model.addAttribute("editId", id);
        addLookups(model);
        return "classes/edit";
    }

    /** POST /classes/{id} – aktualizacja istniejącego wystąpienia po walidacji. */
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("form") ClassOccurrenceForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra) {
        var oc = classOccurrenceRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("success", "Zajęcia nie znalezione (id=" + id + ").");
            return "redirect:/classes";
        }
        Room room = null;
        if (form.getRoomId() != null) {
            room = roomRepository.findById(form.getRoomId()).orElse(null);
        }
        ClassType classType = null;
        if (form.getClassTypeId() != null) {
            classType = classTypeRepository.findById(form.getClassTypeId()).orElse(null);
        }
        if (form.getDurationMinutes() == null || form.getDurationMinutes() <= 0) {
            if (classType != null && classType.getDefaultDurationMinutes() != null && classType.getDefaultDurationMinutes() > 0) {
                form.setDurationMinutes(classType.getDefaultDurationMinutes());
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
            return "classes/edit";
        }

        oc.setType(classTypeRepository.findById(form.getClassTypeId()).orElseThrow());
        oc.setInstructor(userRepository.findById(form.getInstructorId()).orElseThrow());
        oc.setRoom(roomRepository.findById(form.getRoomId()).orElseThrow());

        var zone = ZoneId.of("Europe/Warsaw");
        OffsetDateTime start = LocalDateTime.of(form.getDate(), form.getStartTime()).atZone(zone).toOffsetDateTime();
        OffsetDateTime end = start.plusMinutes(form.getDurationMinutes());
        oc.setStartTime(start);
        oc.setEndTime(end);
        oc.setCapacity(form.getCapacity());
        oc.setNote(form.getNote());

        classOccurrenceRepository.save(oc);
        ra.addFlashAttribute("success", "Zajęcia " + occurrenceLabel(oc) + " zaktualizowane.");
        return "redirect:/classes";
    }

    /** Mapuje encję na formularz (rekonstruuje datę, lokalny czas i duration z różnicy czasów). */
    private ClassOccurrenceForm toForm(ClassOccurrence oc) {
        ClassOccurrenceForm f = new ClassOccurrenceForm();
        f.setClassTypeId(oc.getType().getId());
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
        model.addAttribute("types", classTypeRepository.findAll());
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

    /** POST /classes/{id}/status – zmiana statusu pojedynczego wystąpienia zajęć. */
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") ClassStatus newStatus,
                               RedirectAttributes ra) {
        var oc = classOccurrenceRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("error", "Zajęcia nie znalezione (id=" + id + ").");
            return "redirect:/classes";
        }
        var current = oc.getStatus();
        if (!isAllowedTransition(current, newStatus)) {
            ra.addFlashAttribute("error", "Niepoprawna zmiana statusu z " + current.getLabel() + " na " + newStatus.getLabel() + ".");
            return "redirect:/classes";
        }
        oc.setStatus(newStatus);
        classOccurrenceRepository.save(oc);
        ra.addFlashAttribute("success", "Status zajęć " + occurrenceLabel(oc) + " zmieniony na " + newStatus.getLabel() + ".");
        return "redirect:/classes";
    }

    /** POST /classes/{id}/instructor – zmiana instruktora (zastępstwo) dla pojedynczego wystąpienia. */
    @PostMapping("/{id}/instructor")
    public String updateInstructor(@PathVariable("id") Long id,
                                   @RequestParam("instructorId") Long instructorId,
                                   RedirectAttributes ra) {
        var oc = classOccurrenceRepository.findById(id).orElse(null);
        if (oc == null) {
            ra.addFlashAttribute("error", "Zajęcia nie znalezione (id=" + id + ").");
            return "redirect:/classes";
        }
        if (instructorId == null) {
            ra.addFlashAttribute("error", "Brak identyfikatora instruktora.");
            return "redirect:/classes";
        }
        var newInstr = userRepository.findById(instructorId).orElse(null);
        if (newInstr == null) {
            ra.addFlashAttribute("error", "Instruktor nie znaleziony (id=" + instructorId + ").");
            return "redirect:/classes";
        }
        if (!newInstr.isActive()) {
            ra.addFlashAttribute("error", "Instruktor jest nieaktywny.");
            return "redirect:/classes";
        }
        // Jeśli wybieramy ponownie aktualnego instruktora – brak zmian
        if (oc.getInstructor().getId().equals(instructorId)) {
            ra.addFlashAttribute("success", "Instruktor bez zmian.");
            return "redirect:/classes";
        }
        // Jeśli mamy zastępstwo i wracamy do instruktora pierwotnego – przywróć i wyczyść substitutedFor
        if (oc.getSubstitutedFor() != null && oc.getSubstitutedFor().getId().equals(instructorId)) {
            oc.setInstructor(newInstr); // newInstr to oryginalny
            oc.setSubstitutedFor(null);
            classOccurrenceRepository.save(oc);
            ra.addFlashAttribute("success", "Powrót do instruktora pierwotnego: " + newInstr.getFirstName() + " " + newInstr.getLastName() + ".");
            return "redirect:/classes";
        }
        // Walidacja kolizji czasowej dla nowego instruktora (ignorując bieżące wystąpienie i CANCELLED)
        var conflicts = classOccurrenceRepository.findOverlappingForInstructor(instructorId, oc.getStartTime(), oc.getEndTime())
            .stream().filter(c -> !c.getId().equals(oc.getId()) && c.getStatus() != ClassStatus.CANCELLED).count();
        if (conflicts > 0) {
            ra.addFlashAttribute("error", "Instruktor ma kolizję w tym przedziale czasu.");
            return "redirect:/classes";
        }
        // Ustal pierwotnego instruktora: jeśli jeszcze nie było zastępstwa, zapisz obecnego jako substitutedFor; inaczej pozostaw istniejącego.
        if (oc.getSubstitutedFor() == null) {
            oc.setSubstitutedFor(oc.getInstructor());
        }
        oc.setInstructor(newInstr);
        classOccurrenceRepository.save(oc);
        ra.addFlashAttribute("success", "Instruktor zajęć " + occurrenceLabel(oc) + " zmieniony na: " + newInstr.getFirstName() + " " + newInstr.getLastName() + ".");
        return "redirect:/classes";
    }

    /** Pomocnicza etykieta wystąpienia: [YYYY-MM-DD] Typ HH:mm-HH:mm - Instruktor (Zastępstwo za: Stary Instruktor) */
    private String occurrenceLabel(ClassOccurrence oc) {
        if (oc == null) return "[nieznane]";
        try {
            ZoneId zone = ZoneId.of("Europe/Warsaw");
            LocalDateTime startLocal = oc.getStartTime().atZoneSameInstant(zone).toLocalDateTime();
            LocalDateTime endLocal = oc.getEndTime().atZoneSameInstant(zone).toLocalDateTime();
            DateTimeFormatter dateF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeF = DateTimeFormatter.ofPattern("HH:mm");
            String date = dateF.format(startLocal);
            String times = timeF.format(startLocal) + "-" + timeF.format(endLocal);
            String type = oc.getType() != null ? oc.getType().getName() : "-";
            String instr = oc.getInstructor() != null ? (oc.getInstructor().getFirstName() + " " + oc.getInstructor().getLastName()) : "-";
            String subst = oc.getSubstitutedFor() != null ? " (Zastępstwo za: " + oc.getSubstitutedFor().getFirstName() + " " + oc.getSubstitutedFor().getLastName() + ")" : "";
            return "[" + date + "] "  + type + " " + times +  " - " + instr + subst;
        } catch (Exception ex) {
            return "[nieznane]";
        }
    }
}
