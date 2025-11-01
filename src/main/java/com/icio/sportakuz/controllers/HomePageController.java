package com.icio.sportakuz.controllers;

import com.icio.sportakuz.classes.domain.ClassOccurrence;
import com.icio.sportakuz.classes.domain.ClassStatus;
import com.icio.sportakuz.classes.repo.ClassOccurrenceRepository;
import com.icio.sportakuz.classes.repo.ClassTypeRepository;
import com.icio.sportakuz.classes.repo.InstructorRepository;
import com.icio.sportakuz.classes.repo.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Controller
public class HomePageController {

    private final ClassOccurrenceRepository classOccurrenceRepository;
    private final ClassTypeRepository classTypeRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;

    public HomePageController(ClassOccurrenceRepository classOccurrenceRepository,
                              ClassTypeRepository classTypeRepository,
                              InstructorRepository instructorRepository,
                              RoomRepository roomRepository) {
        this.classOccurrenceRepository = classOccurrenceRepository;
        this.classTypeRepository = classTypeRepository;
        this.instructorRepository = instructorRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        long classesTotal = classOccurrenceRepository.count();
        long typesTotal = classTypeRepository.count();
        long instructorsTotal = instructorRepository.count();
        long roomsTotal = roomRepository.count();

        // Nadchodzące zajęcia (PLANNED, start w przyszłości) – ograniczamy do 5 najbliższych
        OffsetDateTime now = OffsetDateTime.now();
        List<ClassOccurrence> upcoming = classOccurrenceRepository.findAll().stream()
                .filter(c -> c.getStatus() == ClassStatus.PLANNED && c.getStartTime().isAfter(now))
                .sorted(Comparator.comparing(ClassOccurrence::getStartTime))
                .limit(5)
                .toList();

        model.addAttribute("stats_classes", classesTotal);
        model.addAttribute("stats_types", typesTotal);
        model.addAttribute("stats_instructors", instructorsTotal);
        model.addAttribute("stats_rooms", roomsTotal);
        model.addAttribute("upcoming", upcoming);
        return "index"; // /resources/templates/index.html
    }

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("message", "messagemessagemessagemessagemessage");
        return "HomePageView"; // /resources/templates/HomePageView.html
    }
}