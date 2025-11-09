package com.icio.sportakuz.classes.web;

import com.icio.sportakuz.classes.domain.*;
import com.icio.sportakuz.classes.repo.ClassOccurrenceRepository;
import com.icio.sportakuz.classes.repo.ClassSeriesRepository;
import com.icio.sportakuz.classes.repo.ClassTypeRepository;
import com.icio.sportakuz.classes.repo.InstructorRepository;
import com.icio.sportakuz.classes.repo.RoomRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;

/**
 * Kontroler CRUD serii zajęć (ClassSeries) + generowanie wystąpień na podstawie wzorca.
 * Generowanie działa leniwie przy tworzeniu/aktualizacji – tworzymy wystąpienia maks. na 30 dni od teraz
 * lub do daty granicznej serii (w zależności co krótsze).
 */
@Controller
@RequestMapping("/class-series")
public class ClassSeriesController {
    private static final Logger log = LoggerFactory.getLogger(ClassSeriesController.class);

    private final ClassSeriesRepository classSeriesRepository;
    private final ClassOccurrenceRepository classOccurrenceRepository;
    private final ClassTypeRepository classTypeRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;

    public ClassSeriesController(ClassSeriesRepository classSeriesRepository,
                                 ClassOccurrenceRepository classOccurrenceRepository,
                                 ClassTypeRepository classTypeRepository,
                                 InstructorRepository instructorRepository,
                                 RoomRepository roomRepository) {
        this.classSeriesRepository = classSeriesRepository;
        this.classOccurrenceRepository = classOccurrenceRepository;
        this.classTypeRepository = classTypeRepository;
        this.instructorRepository = instructorRepository;
        this.roomRepository = roomRepository;
    }

    /** Lista serii */
    @GetMapping
    public String list(Model model){
        var all = classSeriesRepository.findAll();
        java.util.List<ClassSeries> active = new java.util.ArrayList<>();
        java.util.List<ClassSeries> inactive = new java.util.ArrayList<>();
        for(var s : all){
            if(s.isActive()) active.add(s); else inactive.add(s);
        }
        // sortowanie aktywnych rosnąco po start_time
        active.sort(java.util.Comparator.comparing(ClassSeries::getStartTime));
        // sortowanie nieaktywnych malejąco po recurrence_until (najświeższe u góry)
        inactive.sort(java.util.Comparator.comparing(ClassSeries::getRecurrenceUntil).reversed());
        model.addAttribute("activeSeries", active);
        model.addAttribute("inactiveSeries", inactive);
        model.addAttribute("seriesCount", active.size());
        model.addAttribute("inactiveCount", inactive.size());
        return "classes/series_list";
    }

    /** Formularz nowej serii */
    @GetMapping("/new")
    public String newForm(Model model){
        ClassSeriesForm f = new ClassSeriesForm();
        model.addAttribute("form", f);
        addLookups(model);
        return "classes/new_recurring";
    }

    /** Tworzenie nowej serii */
    @PostMapping
    public String create(@Valid @ModelAttribute("form") ClassSeriesForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra){
        validateDates(form, binding);
        if(binding.hasErrors()){
            addLookups(model);
            return "classes/new_recurring";
        }
        ClassSeries s = new ClassSeries();
        mapToEntity(form, s);
        classSeriesRepository.save(s);
        generateOccurrencesForSeries(s);
        ra.addFlashAttribute("success", "Seria utworzona.");
        return "redirect:/class-series";
    }

    /** Formularz edycji serii */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra){
        var s = classSeriesRepository.findById(id).orElse(null);
        if(s==null){
            ra.addFlashAttribute("success", "Seria nie znaleziona (id="+id+").");
            return "redirect:/class-series";
        }
        model.addAttribute("form", toForm(s));
        model.addAttribute("editId", id);
        addLookups(model);
        return "classes/edit_recurring";
    }

    /** Aktualizacja */
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("form") ClassSeriesForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra){
        var s = classSeriesRepository.findById(id).orElse(null);
        if(s==null){
            ra.addFlashAttribute("error", "Seria nie znaleziona (id="+id+").");
            return "redirect:/class-series";
        }
        var zone = ZoneId.of("Europe/Warsaw");
        LocalDate oldSeriesDate = s.getStartTime() != null ? s.getStartTime().atZoneSameInstant(zone).toLocalDate() : null;
        LocalTime oldSeriesTime = s.getStartTime() != null ? s.getStartTime().atZoneSameInstant(zone).toLocalTime() : null;
        RecurrencePattern oldPattern = s.getRecurrencePattern();
        validateDates(form, binding);
        if(binding.hasErrors()){
            addLookups(model);
            model.addAttribute("editId", id);
            return "classes/edit_recurring";
        }
        boolean patternChanged = oldPattern != form.getRecurrencePattern();
        try {
            if(form.getStartDate()==null || form.getStartTime()==null || form.getUntilDate()==null || form.getRecurrencePattern()==null){
                ra.addFlashAttribute("error", "Brak wymaganych pól (data/godzina/wzorzec/zakres)." );
                return "redirect:/class-series";
            }
            // Zapis nowych parametrów serii
            mapToEntity(form, s);
            classSeriesRepository.save(s);
            OffsetDateTime now = OffsetDateTime.now(zone);
            var occurrences = classOccurrenceRepository.findBySeries_Id(id);
            int updated = 0;
            int removed = 0;
            if(patternChanged){
                // Usuwamy tylko przyszłe aktywne (nie FINISHED/CANCELLED) – unikamy podwójnego dnia po kompresji (DAILY->WEEKLY/MONTHLY) lub ekspansji.
                for(var oc : occurrences){
                    if(oc.getStartTime().isBefore(now)) continue; // przeszłość zostaje
                    if(oc.getStatus()==ClassStatus.CANCELLED || oc.getStatus()==ClassStatus.FINISHED) continue; // archiwalne zostają
                    classOccurrenceRepository.delete(oc);
                    removed++;
                }
                // Regeneracja świeżych wystąpień na podstawie nowego wzorca
                generateOccurrencesForSeries(s);
                ra.addFlashAttribute("success", "Seria zaktualizowana. Wzorzec zmieniony z " + oldPattern + " na " + s.getRecurrencePattern() + ". Usunięto " + removed + " przyszłych wystąpień i wygenerowano nowe.");
                return "redirect:/class-series";
            } else {
                // Remapowanie gdy wzorzec nie zmieniony (np. przesunięcie daty startu / godziny)
                LocalDate newSeriesDate = s.getStartTime().atZoneSameInstant(zone).toLocalDate();
                LocalTime newSeriesTime = s.getStartTime().atZoneSameInstant(zone).toLocalTime();
                long newDurationMinutes = Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
                for(var oc : occurrences){
                    if(oc.getStatus()==ClassStatus.CANCELLED || oc.getStatus()==ClassStatus.FINISHED) continue;
                    if(oc.getStartTime().isBefore(now)) continue;
                    try {
                        LocalDate occOldDate = oc.getStartTime().atZoneSameInstant(zone).toLocalDate();
                        if(oldSeriesDate==null){
                            oldSeriesDate = occOldDate; oldSeriesTime = oc.getStartTime().atZoneSameInstant(zone).toLocalTime();
                        }
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
                        LocalDateTime newStartLocal = LocalDateTime.of(newDate, newSeriesTime);
                        OffsetDateTime newStart = newStartLocal.atZone(zone).toOffsetDateTime();
                        oc.setStartTime(newStart);
                        oc.setEndTime(newStart.plusMinutes(newDurationMinutes));
                        oc.setType(s.getType());
                        oc.setInstructor(s.getInstructor());
                        oc.setRoom(s.getRoom());
                        oc.setCapacity(s.getCapacity());
                        oc.setNote(s.getNote());
                        classOccurrenceRepository.save(oc);
                        updated++;
                    } catch(Exception occEx){
                        log.error("[SERIES][{}] Błąd aktualizacji wystąpienia id={}: {}", id, oc.getId(), occEx.getMessage(), occEx);
                    }
                }
                // Usuń wystąpienia poza zakresem (po remapowaniu) – tylko aktywne
                for(var oc : occurrences){
                    if(oc.getStartTime().isAfter(s.getRecurrenceUntil()) && oc.getStartTime().isAfter(now)){
                        if(oc.getStatus()!=ClassStatus.CANCELLED && oc.getStatus()!=ClassStatus.FINISHED){
                            classOccurrenceRepository.delete(oc);
                            removed++;
                        }
                    }
                }
                generateOccurrencesForSeries(s); // dotwórz brakujące
                ra.addFlashAttribute("success", "Seria zaktualizowana. Zmieniono " + updated + " przyszłych, usunięto " + removed + " poza zakresem.");
                return "redirect:/class-series";
            }
        } catch (Exception ex){
            log.error("[SERIES][{}] Błąd podczas edycji serii: {}", id, ex.getMessage(), ex);
            ra.addFlashAttribute("error", "Nie udało się zaktualizować serii ("+ ex.getClass().getSimpleName() +"). Szczegóły w logach.");
            return "redirect:/class-series";
        }
    }

    /** Usunięcie serii (bez cascade do wystąpień – te pozostają) */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra){
        var s = classSeriesRepository.findById(id).orElse(null);
        if(s!=null){
            // Miękkie usunięcie: zachowujemy rekord dla historycznej spójności i wystąpienia,
            // tylko dezaktywujemy serię (active=false) – brak dalszego generowania.
            if(s.isActive()){
                s.setActive(false);
                classSeriesRepository.save(s);
            }
            ra.addFlashAttribute("success", "Seria oznaczona jako nieaktywna (historyczna). Wystąpienia zachowane.");
        } else {
            ra.addFlashAttribute("success", "Seria nie znaleziona (id="+id+").");
        }
        return "redirect:/class-series";
    }

    /** Aktywacja/dezaktywacja serii (toggle). */
    @PostMapping("/{id}/active")
    public String toggleActive(@PathVariable("id") Long id, @RequestParam("active") boolean active, RedirectAttributes ra){
        var s = classSeriesRepository.findById(id).orElse(null);
        if(s==null){
            ra.addFlashAttribute("success", "Seria nie znaleziona (id="+id+").");
            return "redirect:/class-series";
        }
        s.setActive(active);
        classSeriesRepository.save(s);
        ra.addFlashAttribute("success", "Seria " + id + (active?" aktywowana":" dezaktywowana") + ".");
        return "redirect:/class-series";
    }

    /** Ręczne wymuszenie dotworzenia brakujących wystąpień (do horyzontu 30 dni). */
    @PostMapping("/{id}/generate")
    public String manualGenerate(@PathVariable("id") Long id, RedirectAttributes ra){
        var s = classSeriesRepository.findById(id).orElse(null);
        if(s==null){
            ra.addFlashAttribute("success", "Seria nie znaleziona (id="+id+").");
            return "redirect:/class-series";
        }
        generateOccurrencesForSeries(s);
        ra.addFlashAttribute("success", "Wystąpienia dla serii " + id + " uzupełnione.");
        return "redirect:/class-series";
    }

    private void validateDates(ClassSeriesForm form, BindingResult binding){
        if(form.getStartDate()!=null && form.getUntilDate()!=null){
            if(form.getUntilDate().isBefore(form.getStartDate())){
                binding.rejectValue("untilDate","range","Data końcowa przed startem");
            }
        }
    }

    private void mapToEntity(ClassSeriesForm form, ClassSeries s){
        s.setType(classTypeRepository.findById(form.getClassTypeId()).orElseThrow());
        s.setInstructor(instructorRepository.findById(form.getInstructorId()).orElseThrow());
        s.setRoom(roomRepository.findById(form.getRoomId()).orElseThrow());
        ZoneId zone = ZoneId.of("Europe/Warsaw");
        OffsetDateTime start = LocalDateTime.of(form.getStartDate(), form.getStartTime()).atZone(zone).toOffsetDateTime();
        OffsetDateTime end = start.plusMinutes(form.getDurationMinutes());
        s.setStartTime(start);
        s.setEndTime(end);
        // recurrenceUntil na koniec dnia untilDate (23:59) by generować cały dzień
        OffsetDateTime until = form.getUntilDate().atTime(23,59).atZone(zone).toOffsetDateTime();
        s.setRecurrenceUntil(until);
        s.setRecurrencePattern(form.getRecurrencePattern());
        s.setCapacity(form.getCapacity());
        s.setNote(form.getNote());
        s.setActive(form.isActive());
    }

    private ClassSeriesForm toForm(ClassSeries s){
        ClassSeriesForm f = new ClassSeriesForm();
        f.setClassTypeId(s.getType().getId());
        f.setInstructorId(s.getInstructor().getId());
        f.setRoomId(s.getRoom().getId());
        ZoneId zone = ZoneId.of("Europe/Warsaw");
        f.setStartDate(s.getStartTime().atZoneSameInstant(zone).toLocalDate());
        f.setStartTime(s.getStartTime().atZoneSameInstant(zone).toLocalTime());
        long duration = Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
        f.setDurationMinutes((int) duration);
        f.setUntilDate(s.getRecurrenceUntil().atZoneSameInstant(zone).toLocalDate());
        f.setRecurrencePattern(s.getRecurrencePattern());
        f.setCapacity(s.getCapacity());
        f.setNote(s.getNote());
        f.setActive(s.isActive());
        return f;
    }

    private void addLookups(Model model){
        model.addAttribute("types", classTypeRepository.findAll());
        model.addAttribute("instructors", instructorRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("patterns", RecurrencePattern.values());
    }

    /** Generuje brakujące wystąpienia na podstawie serii w zakresie do min(seria.until, now+30d). */
    private void generateOccurrencesForSeries(ClassSeries s){
        if(!s.isActive()) return; // nie generujemy jeśli nieaktywna
        ZoneId zone = ZoneId.of("Europe/Warsaw");
        OffsetDateTime now = OffsetDateTime.now(zone);
        OffsetDateTime horizon = now.plusDays(30);
        OffsetDateTime limit = s.getRecurrenceUntil().isBefore(horizon) ? s.getRecurrenceUntil() : horizon;
        OffsetDateTime cursor = s.getStartTime();
        int created = 0;
        while(!cursor.isAfter(limit)){
            if(!classOccurrenceRepository.existsBySeries_IdAndStartTime(s.getId(), cursor)){
                ClassOccurrence oc = new ClassOccurrence();
                oc.setSeries(s);
                oc.setType(s.getType());
                oc.setInstructor(s.getInstructor());
                oc.setRoom(s.getRoom());
                oc.setStartTime(cursor);
                oc.setEndTime(cursor.plusMinutes(Duration.between(s.getStartTime(), s.getEndTime()).toMinutes()));
                oc.setCapacity(s.getCapacity());
                oc.setStatus(ClassStatus.PLANNED);
                oc.setNote(s.getNote());
                classOccurrenceRepository.save(oc);
                created++;
            }
            cursor = next(cursor, s.getRecurrencePattern());
        }
        log.info("[SERIES][{}] Dotworzono wystąpień: {}", s.getId(), created);
    }

    private OffsetDateTime next(OffsetDateTime current, RecurrencePattern pattern){
        return switch (pattern){
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
        };
    }
}
