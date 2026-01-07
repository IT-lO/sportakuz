package com.icio.sportakuz.controller.panel;

import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.repo.ActivityRepository;
import com.icio.sportakuz.repo.BookingRepository; // <--- Dodano import
import com.icio.sportakuz.repo.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.ZoneId;
import java.security.Principal;
import java.time.OffsetDateTime;

@Controller
public class UserPanelController {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository; // <--- Dodano pole

    public UserPanelController(ActivityRepository activityRepository,
                               UserRepository userRepository,
                               BookingRepository bookingRepository) { // <--- Dodano do konstruktora
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/panel/user")
    public String index(Model model, Principal principal) {
        String email = principal.getName();

        // Logika nazwy użytkownika
        User currentUser = userRepository.findByEmail(email).orElse(null);
        String displayName = (currentUser != null && currentUser.getFirstName() != null)
                ? currentUser.getFirstName()
                : "Użytkowniku";
        model.addAttribute("userName", displayName);

        OffsetDateTime now = OffsetDateTime.now();

        // --- NOWA LOGIKA STATYSTYK ---

        // 1. Aktywne rezerwacje (wszystkie w przyszłości, nieanulowane)
        long activeBookingsCount = bookingRepository.countActiveBookings(email, now);

        // 2. Odbyte zajęcia (zakończone w ciągu ostatnich 30 dni)
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        long completedBookingsCount = bookingRepository.countCompletedBookings(email, thirtyDaysAgo, now);

        model.addAttribute("userZone", ZoneId.of("Europe/Warsaw"));

        model.addAttribute("stats_active_bookings", activeBookingsCount);
        model.addAttribute("stats_completed_30days", completedBookingsCount);

        // -----------------------------

        // Nadchodzące zajęcia (lista kafelków na dole) - bez zmian
        // Uwaga: Tutaj pobierasz "findNextVisible", co zwraca WSZYSTKIE zajęcia w systemie.
        // Jeśli ta sekcja ma pokazywać tylko zajęcia, na które zapisał się user, trzeba by zmienić zapytanie.
        // Zostawiam jak jest (czyli jako ogólny grafik), zgodnie z Twoim kodem.
        var upcoming = activityRepository.findNextVisible(now, Pageable.ofSize(4));

        model.addAttribute("now", now);
        model.addAttribute("upcoming", upcoming);

        return "panels/user/dashboard";
    }
}