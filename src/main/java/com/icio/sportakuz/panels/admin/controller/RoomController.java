package com.icio.sportakuz.panels.admin.controller;

import com.icio.sportakuz.classes.domain.Room;
import com.icio.sportakuz.classes.repo.RoomRepository;
import com.icio.sportakuz.panels.admin.dto.RoomForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rooms") // Wszystkie adresy w tym kontrolerze będą /rooms/...
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Wyświetla formularz dodawania nowej sali.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Przekazujemy pusty obiekt DTO do powiązania z formularzem
        RoomForm form = new RoomForm();
        form.setActive(true); // Domyślna wartość
        form.setCapacity(10); // Domyślna wartość

        model.addAttribute("roomForm", form);
        model.addAttribute("pageTitle", "Dodaj nową salę");
        return "panels/admin/rooms/form"; // Ścieżka do pliku HTML
    }

    /**
     * Przetwarza dane z formularza dodawania sali.
     */
    @PostMapping("/new")
    public String processCreateForm(@Valid @ModelAttribute("roomForm") RoomForm form,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        // 1. Sprawdzenie, czy sala o tej nazwie już istnieje (unikalność)
        if (roomRepository.findByName(form.getName()).isPresent()) {
            bindingResult.rejectValue("name", "name.duplicate", "Sala o tej nazwie już istnieje.");
        }

        // 2. Sprawdzenie błędów walidacji z DTO (@NotBlank, @Min itd.)
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Dodaj nową salę");
            return "panels/admin/rooms/form"; // Wróć do formularza z błędami
        }

        // 3. Walidacja pomyślna - mapujemy DTO na Encję
        Room newRoom = new Room();
        newRoom.setName(form.getName());
        newRoom.setLocation(form.getLocation());
        newRoom.setCapacity(form.getCapacity());
        newRoom.setActive(form.isActive());

        // 4. Zapis do bazy danych
        roomRepository.save(newRoom);

        // 5. Przekierowanie z komunikatem sukcesu
        // Używamy RedirectAttributes, aby komunikat "przeżył" przekierowanie
        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Sala została pomyślnie dodana.");
        return "redirect:/panel/admin"; // Wróć do głównego panelu admina
    }
}