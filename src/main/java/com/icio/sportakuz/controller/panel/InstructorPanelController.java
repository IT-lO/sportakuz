package com.icio.sportakuz.controller.panel;

import com.icio.sportakuz.entity.User;
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
import java.time.ZoneId;

@Controller
public class InstructorPanelController {

    private final ActivityRepository activityRepository;
    // ActivityTypeRepository nie jest tu konieczne, chyba że używasz do statystyk ogólnych
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public InstructorPanelController(ActivityRepository activityRepository,
                                     ActivityTypeRepository activityTypeRepository,
                                     UserRepository userRepository,
                                     RoomRepository roomRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/panel/instructor")
    public String index(Model model, Principal principal) {
        // 1. Dane zalogowanego instruktora
        String email = principal.getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);

        String displayName = (currentUser != null && currentUser.getFirstName() != null)
                ? currentUser.getFirstName()
                : "Instruktorze";

        model.addAttribute("userName", displayName);

        // 2. Konfiguracja czasu
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        model.addAttribute("userZone", ZoneId.of("Europe/Warsaw"));
        model.addAttribute("now", now);

        // --- ZMIANA: Statystyki specyficzne dla Instruktora ---

        // A. Odbyte zajęcia (poprowadzone przez niego w ostatnich 30 dniach)
        long completedCount = activityRepository.countCompletedByInstructor(email, thirtyDaysAgo, now);
        model.addAttribute("stats_completed_30days", completedCount);

        // B. Nadchodzące zajęcia (tylko te, które ON prowadzi)
        // Zamiast findNextVisible (wszystkie), używamy findUpcomingForInstructor
        var myUpcoming = activityRepository.findUpcomingForInstructor(email, now, Pageable.ofSize(4));
        model.addAttribute("upcoming", myUpcoming);

        // C. Liczba nadchodzących (jako "Rezerwacje/Zajęcia do zrobienia")
        // Możemy użyć rozmiaru listy lub osobnego counta, jeśli lista jest stronicowana
        model.addAttribute("stats_active_bookings", myUpcoming.size());

        // D. Inne statystyki (np. dostępne sale - opcjonalne)
        long roomsTotal = roomRepository.count();
        model.addAttribute("stats_rooms", roomsTotal);

        return "panels/instructor/dashboard";
    }
}