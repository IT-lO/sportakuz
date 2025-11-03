package com.icio.sportakuz.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.util.List;

/**
 * Kontroler testowy ("probe") służący do szybkiej weryfikacji działania - można usunąć.
 * Wstawia do modelu przykładowe dane (suma, lista imion) renderowane w szablonie probe.html.
 */
@Controller
public class ProbeController {
    @GetMapping("/probe")
    public String probe(Model model) {
        model.addAttribute("sum", 2 + 2);
        model.addAttribute("names", List.of("Ala", "Ola", "Ela"));
        return "probe"; // Thymeleaf: templates/probe.html
    }
}
