package com.icio.sportakuz.rooms.controller;

import com.icio.sportakuz.classes.domain.Room;
import com.icio.sportakuz.classes.repo.RoomRepository;
import com.icio.sportakuz.rooms.dto.RoomForm;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Wyświetla listę wszystkich sal.
     */
    @GetMapping
    public String showRoomList(Model model) {
        List<Room> rooms = roomRepository.findAll(Sort.by("name"));
        model.addAttribute("rooms", rooms);
        model.addAttribute("pageTitle", "Sale i Pomieszczenia");
        return "rooms/list_rooms";
    }

    /**
     * Wyświetla formularz dodawania nowej sali.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        RoomForm form = new RoomForm();
        form.setActive(true);
        form.setCapacity(10);

        model.addAttribute("roomForm", form);
        model.addAttribute("pageTitle", "Dodaj nową salę");
        return "rooms/form_add_room";
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
            return "rooms/form_add_room";
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
        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Sala została pomyślnie dodana.");
        return "redirect:/rooms";
    }

    /**
     * Wyświetla formularz edycji sali.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Room> roomOpt = roomRepository.findById(id);

        if (roomOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie znaleziono sali o ID: " + id);
            return "redirect:/rooms";
        }

        Room room = roomOpt.get();
        RoomForm form = new RoomForm();
        form.setName(room.getName());
        form.setLocation(room.getLocation());
        form.setCapacity(room.getCapacity());
        form.setActive(room.isActive());

        model.addAttribute("roomForm", form);
        model.addAttribute("roomId", id);
        model.addAttribute("pageTitle", "Edytuj salę: " + room.getName());
        return "rooms/form_edit_room";
    }

    /**
     * Przetwarza dane z formularza edycji sali.
     */
    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable("id") Long id,
                                  @Valid @ModelAttribute("roomForm") RoomForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        Optional<Room> roomOpt = roomRepository.findById(id);
        if (roomOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Błąd edycji: Nie znaleziono sali.");
            return "redirect:/rooms";
        }

        // 1. Sprawdzenie unikalności nazwy (innej niż własna)
        Optional<Room> existingName = roomRepository.findByName(form.getName());
        if (existingName.isPresent() && !existingName.get().getId().equals(id)) {
            bindingResult.rejectValue("name", "name.duplicate", "Inna sala używa już tej nazwy.");
        }

        // 2. Sprawdzenie błędów walidacji
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edytuj salę");
            model.addAttribute("roomId", id); // Musimy ponownie przekazać ID
            return "rooms/form_edit_room";
        }

        // 3. Mapowanie DTO na Encję
        Room roomToUpdate = roomOpt.get();
        roomToUpdate.setName(form.getName());
        roomToUpdate.setLocation(form.getLocation());
        roomToUpdate.setCapacity(form.getCapacity());
        roomToUpdate.setActive(form.isActive());

        // 4. Zapis
        roomRepository.save(roomToUpdate);

        // 5. Przekierowanie
        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Dane sali zostały pomyślnie zaktualizowane.");
        return "redirect:/rooms";
    }

    /**
     * Przetwarza żądanie usunięcia sali.
     */
    @PostMapping("/delete/{id}")
    public String processDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        // 1. Sprawdzenie, czy sala istnieje
        Optional<Room> roomOpt = roomRepository.findById(id);
        if (roomOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie znaleziono sali do usunięcia (ID: " + id + ").");
            return "redirect:/rooms";
        }

        // 2. Próba usunięcia z obsługą błędu więzów integralności
        // (Jeśli sala jest przypisana do jakichś zajęć, baza danych rzuci błędem)
        try {
            roomRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("globalSuccessMessage", "Sala została pomyślnie usunięta.");

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage",
                    "Nie można usunąć tej sali, ponieważ jest ona powiązana z istniejącymi zajęciami. Najpierw usuń lub zmień powiązane zajęcia.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Wystąpił nieoczekiwany błąd podczas usuwania sali.");
        }

        // 3. Powrót do listy
        return "redirect:/rooms";
    }
}