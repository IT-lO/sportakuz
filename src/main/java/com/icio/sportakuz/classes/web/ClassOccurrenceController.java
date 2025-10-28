// src/main/java/com/icio/sportakuz/classes/web/ClassOccurrenceController.java
package com.icio.sportakuz.classes.web;

import com.icio.sportakuz.classes.domain.*; // encje
import com.icio.sportakuz.classes.repo.*;   // repozytoria
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
@RequestMapping("/classes")
public class ClassOccurrenceController {

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
        model.addAttribute("form", new ClassOccurrenceForm());
        addLookups(model);
        return "classes/new";
    }

    // ZAPIS
    @PostMapping
    public String create(@Valid @ModelAttribute("form") ClassOccurrenceForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra) {

        // prosta walidacja: start < end
        if (form.getStartTime() != null && form.getEndTime() != null
                && !form.getStartTime().isBefore(form.getEndTime())) {
            binding.rejectValue("endTime", "time.order", "Godzina zakończenia musi być po rozpoczęciu");
        }

        // sprawdzenie capacity vs room.capacity – lepiej złapać to przed constraintem z DB
        if (form.getRoomId() != null && form.getCapacity() != null) {
            Room room = roomRepository.findById(form.getRoomId()).orElse(null);
            if (room != null && form.getCapacity() > room.getCapacity()) {
                binding.rejectValue("capacity", "capacity.room",
                        "Pojemność zajęć nie może przekraczać pojemności sali (" + room.getCapacity() + ")");
            }
        }

        if (binding.hasErrors()) {
            addLookups(model);
            return "classes/new";
        }

        // mapowanie form -> encja
        ClassOccurrence oc = new ClassOccurrence();
        oc.setType(classTypeRepository.findById(form.getClassTypeId()).orElseThrow());
        oc.setInstructor(instructorRepository.findById(form.getInstructorId()).orElseThrow());
        oc.setRoom(roomRepository.findById(form.getRoomId()).orElseThrow());

        // budujemy LocalDateTime z date + times (strefa serwera)
        var zone = ZoneId.of("Europe/Warsaw");
        var start = LocalDateTime.of(form.getDate(), form.getStartTime())
                .atZone(zone)
                .toOffsetDateTime();
        var end   = LocalDateTime.of(form.getDate(), form.getEndTime())
                .atZone(zone)
                .toOffsetDateTime();
        oc.setStartTime(start);
        oc.setEndTime(end);

        oc.setCapacity(form.getCapacity());
        oc.setStatus(ClassStatus.PLANNED); // na start
        oc.setNote(form.getNote());

        classOccurrenceRepository.save(oc);

        ra.addFlashAttribute("success", "Zajęcia dodane.");
        return "redirect:/classes";
    }

    private void addLookups(Model model) {
        model.addAttribute("types", classTypeRepository.findAll());
        model.addAttribute("instructors", instructorRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
    }
}
