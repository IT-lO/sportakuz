package com.icio.sportakuz.controller.panel;

import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.repo.ActivityRepository;
import com.icio.sportakuz.repo.BookingRepository;
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
    private final BookingRepository bookingRepository;

    public UserPanelController(ActivityRepository activityRepository,
                               UserRepository userRepository,
                               BookingRepository bookingRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/panel/user")
    public String index(Model model, Principal principal) {
        String email = principal.getName();

        User currentUser = userRepository.findByEmail(email).orElse(null);
        String displayName = (currentUser != null && currentUser.getFirstName() != null)
                ? currentUser.getFirstName()
                : "Użytkowniku";
        model.addAttribute("userName", displayName);

        OffsetDateTime now = OffsetDateTime.now();

        long activeBookingsCount = bookingRepository.countActiveBookings(email, now);

        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        long completedBookingsCount = bookingRepository.countCompletedBookings(email, thirtyDaysAgo, now);

        model.addAttribute("userZone", ZoneId.of("Europe/Warsaw"));

        model.addAttribute("stats_active_bookings", activeBookingsCount);
        model.addAttribute("stats_completed_30days", completedBookingsCount);

        var upcoming = activityRepository.findNextVisible(now, Pageable.ofSize(4));

        model.addAttribute("now", now);
        model.addAttribute("upcoming", upcoming);

        return "panels/user/dashboard";
    }
}