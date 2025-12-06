package com.icio.sportakuz.controller.panel;

import com.icio.sportakuz.entity.UserRole;
import com.icio.sportakuz.repo.ClassOccurrenceRepository;
import com.icio.sportakuz.repo.ActivityTypeRepository;
import com.icio.sportakuz.repo.RoomRepository;
import com.icio.sportakuz.repo.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.OffsetDateTime;

/**
 * Kontroler panelu administracyjnego. Odpowiada za możliwość dodawnia oraz edycji zasobów
 * takich jak typy zajęć, sale czy instruktorzy.
 * Zbiera statystyki (liczby zajęć, typów, instruktorów, sal)
 * oraz listy najbliższych widocznych zajęć (upcoming). Dane trafiają do szablonu panel/admin.
 */
@Controller
public class AdminPanelController {

    private final ClassOccurrenceRepository classOccurrenceRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public AdminPanelController(ClassOccurrenceRepository classOccurrenceRepository,
                              ActivityTypeRepository activityTypeRepository,
                              UserRepository userRepository,
                              RoomRepository roomRepository) {
        this.classOccurrenceRepository = classOccurrenceRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    /** Panel Administratora – Udostępnia administratorowi możliwość podglądu wszystkich zajęć oraz instruktorów */
    @GetMapping("/panel/admin")
    public String index(Model model) {
        long classesTotal = classOccurrenceRepository.count();
        long typesTotal = activityTypeRepository.count();
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
        return "panels/admin/dashboard"; // /resources/templates/panels/admin/dashboard.html
    }
}