package com.icio.sportakuz.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProbeController {
    @GetMapping("/probe")
    public String probe() {
        return "probe"; // => /WEB-INF/views/probe.jsp
    }
}
