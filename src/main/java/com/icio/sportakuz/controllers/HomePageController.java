package com.icio.sportakuz.controllers;

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

        OffsetDateTime now = OffsetDateTime.now();
        var upcoming = classOccurrenceRepository.findNextVisible(now, Pageable.ofSize(4));
        model.addAttribute("now", now);
        model.addAttribute("upcoming", upcoming);

        model.addAttribute("stats_classes", classesTotal);
        model.addAttribute("stats_types", typesTotal);
        model.addAttribute("stats_instructors", instructorsTotal);
        model.addAttribute("stats_rooms", roomsTotal);
        return "index"; // /resources/templates/index.html
    }

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("message", "messagemessagemessagemessagemessage");
        return "HomePageView"; // /resources/templates/HomePageView.html
    }
}