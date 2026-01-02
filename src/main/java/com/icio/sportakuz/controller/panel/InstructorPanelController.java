package com.icio.sportakuz.controller.panel;

import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.entity.UserRole;
import com.icio.sportakuz.repo.ActivityRepository;
import com.icio.sportakuz.repo.ActivityTypeRepository;
import com.icio.sportakuz.repo.RoomRepository;
import com.icio.sportakuz.repo.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.OffsetDateTime;

@Controller
public class InstructorPanelController {

    private final ActivityRepository activityRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public InstructorPanelController(ActivityRepository activityRepository,
                                     ActivityTypeRepository activityTypeRepository,
                                     UserRepository userRepository,
                                     RoomRepository roomRepository) {
        this.activityRepository = activityRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/panel/instructor")
    public String index(Model model, Principal principal) {
        // 1. Pobieramy email zalogowanego użytkownika
        String email = principal.getName();

        // 2. Szukamy użytkownika w bazie, aby pobrać jego imię
        User currentUser = userRepository.findByEmail(email).orElse(null);

        // 3. Ustawiamy zmienną dla widoku (jeśli brak imienia, fallback do "Użytkowniku")
        String displayName = (currentUser != null && currentUser.getFirstName() != null)
                ? currentUser.getFirstName()
                : "Użytkowniku";

        model.addAttribute("userName", displayName);

        long roomsTotal = roomRepository.count();

        OffsetDateTime now = OffsetDateTime.now();
        var upcoming = activityRepository.findNextVisible(now, Pageable.ofSize(4));
        model.addAttribute("now", now);
        model.addAttribute("upcoming", upcoming);

        model.addAttribute("stats_rooms", roomsTotal);

        return "panels/instructor/dashboard";
    }
}