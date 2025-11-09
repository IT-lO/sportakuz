package com.icio.sportakuz.panels.admin.controller;

import com.icio.sportakuz.classes.domain.ClassOccurrence;
import com.icio.sportakuz.classes.repo.ClassOccurrenceRepository;
import com.icio.sportakuz.classes.repo.ClassTypeRepository;
import com.icio.sportakuz.classes.repo.InstructorRepository;
import com.icio.sportakuz.classes.repo.RoomRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.OffsetDateTime;

/**
 * Kontroler strony głównej. Odpowiada za zebranie statystyk (liczby zajęć, typów, instruktorów, sal)
 * oraz listy najbliższych widocznych zajęć (upcoming). Dane trafiają do szablonu index.html.
 * Udostępnia także prosty endpoint /hello do testu renderowania innego widoku.
 */
@Controller
public class AdminPanelController {

    private final ClassOccurrenceRepository classOccurrenceRepository;
    private final ClassTypeRepository classTypeRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;

    public AdminPanelController(ClassOccurrenceRepository classOccurrenceRepository,
                              ClassTypeRepository classTypeRepository,
                              InstructorRepository instructorRepository,
                              RoomRepository roomRepository) {
        this.classOccurrenceRepository = classOccurrenceRepository;
        this.classTypeRepository = classTypeRepository;
        this.instructorRepository = instructorRepository;
        this.roomRepository = roomRepository;
    }

    /** Panel Administratora – Udostępnia administratorowi możliwość podglądu wszystkich zajęć oraz instruktorów */
    @GetMapping("/panel/admin")
    public String index(Model model) {
        long classesTotal = classOccurrenceRepository.count();
        long typesTotal = classTypeRepository.count();
        long instructorsTotal = instructorRepository.count();
        long roomsTotal = roomRepository.count();

        OffsetDateTime now = OffsetDateTime.now();
        var upcoming = classOccurrenceRepository.findNextVisible(now, Pageable.ofSize(4));
        model.addAttribute("now", now);
        model.addAttribute("upcoming", upcoming);

        model.addAttribute("stats_classes", classesTotal);
        model.addAttribute("stats_types", typesTotal);
        model.addAttribute("stats_instructors", instructorsTotal);
        model.addAttribute("stats_rooms", roomsTotal);
        return "panels/admin/dashboard"; // /resources/templates/panels/admin/dashboard.html
    }
}