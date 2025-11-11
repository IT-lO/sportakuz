package com.icio.sportakuz.instructors.controller;

import com.icio.sportakuz.classes.domain.Instructor;
import com.icio.sportakuz.classes.repo.InstructorRepository;
import com.icio.sportakuz.instructors.dto.InstructorForm;
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
@RequestMapping("/instructors")
public class InstructorController {

    private final InstructorRepository instructorRepository;

    public InstructorController(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    /**
     * Wyświetla listę wszystkich instruktorów.
     */
    @GetMapping
    public String showInstructorList(Model model) {
        List<Instructor> instructors = instructorRepository.findAll(Sort.by("lastName", "firstName"));
        model.addAttribute("instructors", instructors);
        model.addAttribute("pageTitle", "Instruktorzy");
        return "instructors/list_instructors";
    }

    /**
     * Wyświetla formularz dodawania nowego instruktora.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        InstructorForm form = new InstructorForm();
        form.setActive(true);

        model.addAttribute("instructorForm", form);
        model.addAttribute("pageTitle", "Dodaj nowego instruktora");
        return "instructors/form_add_instructor";
    }

    /**
     * Przetwarza dane z formularza dodawania instruktora.
     */
    @PostMapping("/new")
    public String processCreateForm(@Valid @ModelAttribute("instructorForm") InstructorForm form,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        // 1. Sprawdzenie unikalności e-maila
        if (instructorRepository.findByEmail(form.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "email.duplicate", "Instruktor o tym adresie e-mail już istnieje.");
        }

        // 2. Sprawdzenie błędów walidacji
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Dodaj nowego instruktora");
            return "instructors/form_add_instructor";
        }

        // 3. Mapowanie DTO na Encję
        Instructor newInstructor = new Instructor();
        newInstructor.setFirstName(form.getFirstName());
        newInstructor.setLastName(form.getLastName());
        newInstructor.setEmail(form.getEmail());
        newInstructor.setPhone(form.getPhone());
        newInstructor.setBio(form.getBio());
        newInstructor.setActive(form.isActive());

        // 4. Zapis
        instructorRepository.save(newInstructor);

        // 5. Przekierowanie
        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Instruktor został pomyślnie dodany.");
        return "redirect:/instructors"; // Przekierowanie na listę
    }

    /**
     * Wyświetla formularz edycji instruktora.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Instructor> instructorOpt = instructorRepository.findById(id);

        if (instructorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie znaleziono instruktora o ID: " + id);
            return "redirect:/instructors";
        }

        // Mapowanie Encji na DTO (formularz)
        Instructor instructor = instructorOpt.get();
        InstructorForm form = new InstructorForm();
        form.setFirstName(instructor.getFirstName());
        form.setLastName(instructor.getLastName());
        form.setEmail(instructor.getEmail());
        form.setPhone(instructor.getPhone());
        form.setBio(instructor.getBio());
        form.setActive(instructor.isActive());

        model.addAttribute("instructorForm", form);
        model.addAttribute("instructorId", id);
        model.addAttribute("pageTitle", "Edytuj instruktora: " + instructor.getFirstName() + " " + instructor.getLastName());
        return "instructors/form_edit_instructor";
    }

    /**
     * Przetwarza dane z formularza edycji instruktora.
     */
    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable("id") Long id,
                                  @Valid @ModelAttribute("instructorForm") InstructorForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        Optional<Instructor> instructorOpt = instructorRepository.findById(id);
        if (instructorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Błąd edycji: Nie znaleziono instruktora.");
            return "redirect:/instructors";
        }

        // 1. Sprawdzenie unikalności e-maila (innego niż własny)
        Optional<Instructor> existingEmail = instructorRepository.findByEmail(form.getEmail());
        if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
            bindingResult.rejectValue("email", "email.duplicate", "Inny instruktor używa już tego adresu e-mail.");
        }

        // 2. Sprawdzenie błędów walidacji
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edytuj instruktora");
            model.addAttribute("instructorId", id); // Musimy ponownie przekazać ID
            return "instructors/form_edit_instructor";
        }

        // 3. Mapowanie DTO na Encję
        Instructor instructorToUpdate = instructorOpt.get();
        instructorToUpdate.setFirstName(form.getFirstName());
        instructorToUpdate.setLastName(form.getLastName());
        instructorToUpdate.setEmail(form.getEmail());
        instructorToUpdate.setPhone(form.getPhone());
        instructorToUpdate.setBio(form.getBio());
        instructorToUpdate.setActive(form.isActive());

        // 4. Zapis
        instructorRepository.save(instructorToUpdate);

        // 5. Przekierowanie
        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Dane instruktora zostały pomyślnie zaktualizowane.");
        return "redirect:/instructors";
    }

    /**
     * Przetwarza żądanie usunięcia instruktora.
     */
    @PostMapping("/delete/{id}")
    public String processDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        // 1. Sprawdzenie, czy instruktor istnieje
        Optional<Instructor> instructorOpt = instructorRepository.findById(id);
        if (instructorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie znaleziono instruktora do usunięcia (ID: " + id + ").");
            return "redirect:/instructors";
        }

        // 2. Próba usunięcia z obsługą błędu więzów integralności
        // (Jeśli instruktor jest przypisany do jakichś zajęć, baza danych rzuci błędem)
        try {
            instructorRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("globalSuccessMessage", "Instruktor został pomyślnie usunięty.");

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage",
                    "Nie można usunąć tego instruktora, ponieważ jest on powiązany z istniejącymi zajęciami. Najpierw usuń lub zmień powiązane zajęcia.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Wystąpił nieoczekiwany błąd podczas usuwania instruktora.");
        }

        // 3. Powrót do listy
        return "redirect:/instructors";
    }
}