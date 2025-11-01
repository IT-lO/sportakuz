// src/main/java/com/icio/sportakuz/classes/web/ClassOccurrenceController.java
package com.icio.sportakuz.classes.web;

import com.icio.sportakuz.classes.domain.*;
import com.icio.sportakuz.classes.repo.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Controller
@RequestMapping("/classes")
public class ClassOccurrenceController {
    private static final Logger log = LoggerFactory.getLogger(ClassOccurrenceController.class);

    private final ClassOccurrenceRepository classOccurrenceRepository;
    private final ClassTypeRepository classTypeRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;

    public ClassOccurrenceController(ClassOccurrenceRepository classOccurrenceRepository,
                                     ClassTypeRepository classTypeRepository,
                                     InstructorRepository instructorRepository,
                                     RoomRepository roomRepository) {
        this.classOccurrenceRepository = classOccurrenceRepository;
        this.classTypeRepository = classTypeRepository;
        this.instructorRepository = instructorRepository;
        this.roomRepository = roomRepository;
    }

    // LISTA
    @GetMapping
    public String list(Model model) {
        model.addAttribute("classes", classOccurrenceRepository.findAll()); // na start prosto
        return "classes/list";
    }

    // FORMULARZ
    @GetMapping("/new")
    public String createForm(Model model) {
        ClassOccurrenceForm f = new ClassOccurrenceForm();
        f.setDurationMinutes(55); // domyślny czas trwania 55 min
        model.addAttribute("form", f);
        addLookups(model);
        return "classes/new";
    }

    private OffsetDateTime computeStart(ClassOccurrenceForm form) {
        if (form.getDate() == null || form.getStartTime() == null) return null;
        var zone = ZoneId.of("Europe/Warsaw");
        return LocalDateTime.of(form.getDate(), form.getStartTime()).atZone(zone).toOffsetDateTime();
    }

    private OffsetDateTime computeEnd(OffsetDateTime start, Integer durationMinutes) {
        if (start == null || durationMinutes == null || durationMinutes <= 0) return null;
        return start.plusMinutes(durationMinutes);
    }

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

    // ZAPIS
    @PostMapping
    public String create(@Valid @ModelAttribute("form") ClassOccurrenceForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra) {

        Room room = null;
        if (form.getRoomId() != null) {
            room = roomRepository.findById(form.getRoomId()).orElse(null);
        }
        if (form.getDurationMinutes() == null || form.getDurationMinutes() <= 0) {
            form.setDurationMinutes(60);
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
        oc.setInstructor(instructorRepository.findById(form.getInstructorId()).orElseThrow());
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

    // USUWANIE
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        if (id != null && classOccurrenceRepository.existsById(id)) {
            classOccurrenceRepository.deleteById(id);
            ra.addFlashAttribute("success", "Zajęcia #" + id + " usunięte.");
        } else {
            ra.addFlashAttribute("success", "Zajęcia nie znalezione (id=" + id + ").");
        }
        return "redirect:/classes";
    }

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
        if (form.getDurationMinutes() == null || form.getDurationMinutes() <= 0) {
            form.setDurationMinutes(60);
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
        oc.setInstructor(instructorRepository.findById(form.getInstructorId()).orElseThrow());
        oc.setRoom(roomRepository.findById(form.getRoomId()).orElseThrow());

        var zone = ZoneId.of("Europe/Warsaw");
        OffsetDateTime start = LocalDateTime.of(form.getDate(), form.getStartTime()).atZone(zone).toOffsetDateTime();
        OffsetDateTime end = start.plusMinutes(form.getDurationMinutes());
        oc.setStartTime(start);
        oc.setEndTime(end);
        oc.setCapacity(form.getCapacity());
        oc.setNote(form.getNote());

        classOccurrenceRepository.save(oc);
        ra.addFlashAttribute("success", "Zajęcia zaktualizowane.");
        return "redirect:/classes";
    }

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

    private void addLookups(Model model) {
        model.addAttribute("types", classTypeRepository.findAll());
        model.addAttribute("instructors", instructorRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
    }
}
