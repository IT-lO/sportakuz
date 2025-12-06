package com.icio.sportakuz.controller.panel;

import com.icio.sportakuz.entity.UserRole;
import com.icio.sportakuz.repo.ActivityRepository;
import com.icio.sportakuz.repo.ActivityTypeRepository;
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

    private final ActivityRepository activityRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public UserPanelController(ActivityRepository activityRepository,
                               ActivityTypeRepository activityTypeRepository,
                               UserRepository userRepository,
                               RoomRepository roomRepository) {
        this.activityRepository = activityRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    /** Panel Użytkownika – Udostępnia użytkownikowi możliwość podglądu jego rezerwacji */
    @GetMapping("/panel/user")
    public String index(Model model) {
        long classesTotal = activityRepository.count();
        long typesTotal = activityTypeRepository.count();
        long instructorsTotal = userRepository.countByRole(UserRole.ROLE_INSTRUCTOR);
        long roomsTotal = roomRepository.count();

        OffsetDateTime now = OffsetDateTime.now();
        var upcoming = activityRepository.findNextVisible(now, Pageable.ofSize(4));
        model.addAttribute("now", now);
        model.addAttribute("upcoming", upcoming);

        model.addAttribute("stats_classes", classesTotal);
        model.addAttribute("stats_types", typesTotal);
        model.addAttribute("stats_instructors", instructorsTotal);
        model.addAttribute("stats_rooms", roomsTotal);
        return "panels/user/dashboard"; // /resources/templates/panels/user/dashboard.html
    }
}