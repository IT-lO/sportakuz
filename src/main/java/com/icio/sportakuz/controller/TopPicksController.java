package com.icio.sportakuz.controller;

import com.icio.sportakuz.entity.Activity;
import com.icio.sportakuz.repo.ActivityRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/top-picks")
public class TopPicksController {

    private final ActivityRepository activityRepository;

    public TopPicksController(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @GetMapping
    public String showTopPicks(Model model) {
        List<Activity> topPicks = activityRepository.findTopPicks(OffsetDateTime.now());

        model.addAttribute("topPicks", topPicks);
        model.addAttribute("userZone", ZoneId.of("Europe/Warsaw"));

        return "toppicks/top_picks";
    }
}