package com.icio.sportakuz.controller.panel;

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
 * Kontroler panelu użytkownika.
 * Zbiera statystyki użytkownika (zajęcia w których uczestniczy itp.)
 * oraz listy najbliższych widocznych zajęć (upcoming). Dane trafiają do szablonu panel/user.
 */
@Controller
public class UserPanelController {

    private final ClassOccurrenceRepository classOccurrenceRepository;
    private final ClassTypeRepository classTypeRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public UserPanelController(ClassOccurrenceRepository classOccurrenceRepository,
                                ClassTypeRepository classTypeRepository,
                               UserRepository userRepository,
                                RoomRepository roomRepository) {
        this.classOccurrenceRepository = classOccurrenceRepository;
        this.classTypeRepository = classTypeRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    /** Panel Użytkownika – Udostępnia użytkownikowi możliwość podglądu jego rezerwacji */
    @GetMapping("/panel/user")
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
        return "panels/user/dashboard"; // /resources/templates/panels/user/dashboard.html
    }
}