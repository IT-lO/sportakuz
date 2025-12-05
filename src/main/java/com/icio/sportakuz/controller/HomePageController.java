package com.icio.sportakuz.controller;

import com.icio.sportakuz.entity.UserRole;
import com.icio.sportakuz.repo.ClassOccurrenceRepository;
import com.icio.sportakuz.repo.ClassTypeRepository;
import com.icio.sportakuz.repo.RoomRepository;
import com.icio.sportakuz.repo.UserRepository;
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
public class HomePageController {

    private final ClassOccurrenceRepository classOccurrenceRepository;
    private final ClassTypeRepository classTypeRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public HomePageController(ClassOccurrenceRepository classOccurrenceRepository,
                              ClassTypeRepository classTypeRepository,
                              RoomRepository roomRepository, UserRepository userRepository) {
        this.classOccurrenceRepository = classOccurrenceRepository;
        this.classTypeRepository = classTypeRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    /** Strona główna – pobiera statystyki i najbliższe zajęcia (limit 4). */
    @GetMapping("/")
    public String index(Model model) {
        long classesTotal = classOccurrenceRepository.count();
        long typesTotal = classTypeRepository.count();
        long instructorsTotal = userRepository.countByRole(UserRole.ROLE_INSTRUCTOR);
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
}