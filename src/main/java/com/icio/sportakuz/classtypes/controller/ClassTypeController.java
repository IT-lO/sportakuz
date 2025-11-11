package com.icio.sportakuz.classtypes.controller;

import com.icio.sportakuz.classes.domain.ClassType;
import com.icio.sportakuz.classes.repo.ClassTypeRepository;
import com.icio.sportakuz.classtypes.dto.ClassTypeForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/classtypes") // Wszystkie adresy w tym kontrolerze będą /classtypes/...
public class ClassTypeController {

    private final ClassTypeRepository classTypeRepository;

    public ClassTypeController(ClassTypeRepository classTypeRepository) {
        this.classTypeRepository = classTypeRepository;
    }

    /**
     * Wyświetla listę wszystkich typów zajęć.
     */
    @GetMapping
    public String showClassTypeList(Model model) {
        List<ClassType> classTypes = classTypeRepository.findAll(Sort.by("name"));
        model.addAttribute("classTypes", classTypes);
        model.addAttribute("pageTitle", "Typy Zajęć");
        return "classtypes/list_classtypes"; // Ścieżka do nowej listy
    }


    /**
     * Wyświetla formularz dodawania nowego typu zajęć.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("classTypeForm", new ClassTypeForm());
        model.addAttribute("pageTitle", "Dodaj nowy typ zajęć");
        return "classtypes/form_add_classtype";
    }

    /**
     * Przetwarza dane z formularza dodawania typu zajęć.
     */
    @PostMapping("/new")
    public String processCreateForm(@Valid @ModelAttribute("classTypeForm") ClassTypeForm form,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        // 1. Sprawdzenie, czy typ o tej nazwie już istnieje (unikalność)
        if (classTypeRepository.findByName(form.getName()).isPresent()) {
            bindingResult.rejectValue("name", "name.duplicate", "Typ zajęć o tej nazwie już istnieje.");
        }

        // 2. Sprawdzenie błędów walidacji z DTO (@NotBlank, @Size itd.)
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Dodaj nowy typ zajęć");
            return "classtypes/form_add_classtype";
        }

        // 3. Walidacja pomyślna - mapujemy DTO na Encję
        ClassType newType = new ClassType();
        newType.setName(form.getName());
        newType.setDescription(form.getDescription());
        newType.setDefaultDurationMinutes(form.getDefaultDurationMinutes());
        newType.setDifficulty(form.getDifficulty());

        // 4. Zapis do bazy danych
        classTypeRepository.save(newType);

        // 5. Przekierowanie z komunikatem sukcesu
        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Nowy typ zajęć został pomyślnie dodany.");
        return "redirect:/classtypes"; // Wróć do listy typów zajęć
    }

    /**
     * Wyświetla formularz edycji typu zajęć.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ClassType> classTypeOpt = classTypeRepository.findById(id);

        if (classTypeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie znaleziono typu zajęć o ID: " + id);
            return "redirect:/classtypes";
        }

        // Mapowanie Encji na DTO (formularz)
        ClassType classType = classTypeOpt.get();
        ClassTypeForm form = new ClassTypeForm();
        form.setName(classType.getName());
        form.setDescription(classType.getDescription());
        form.setDefaultDurationMinutes(classType.getDefaultDurationMinutes());
        form.setDifficulty(classType.getDifficulty());

        model.addAttribute("classTypeForm", form);
        model.addAttribute("classTypeId", id); // Potrzebne do action w formularzu
        model.addAttribute("pageTitle", "Edytuj typ zajęć: " + classType.getName());
        return "classtypes/form_edit_classtype"; // Ścieżka do nowego formularza edycji
    }

    /**
     * Przetwarza dane z formularza edycji typu zajęć.
     */
    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable("id") Long id,
                                  @Valid @ModelAttribute("classTypeForm") ClassTypeForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        Optional<ClassType> classTypeOpt = classTypeRepository.findById(id);
        if (classTypeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Błąd edycji: Nie znaleziono typu zajęć.");
            return "redirect:/classtypes";
        }

        // 1. Sprawdzenie unikalności nazwy (innej niż własna)
        Optional<ClassType> existingName = classTypeRepository.findByName(form.getName());
        if (existingName.isPresent() && !existingName.get().getId().equals(id)) {
            bindingResult.rejectValue("name", "name.duplicate", "Inny typ zajęć używa już tej nazwy.");
        }

        // 2. Sprawdzenie błędów walidacji
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edytuj typ zajęć");
            model.addAttribute("classTypeId", id); // Musimy ponownie przekazać ID
            return "classtypes/form_edit_classtype";
        }

        // 3. Mapowanie DTO na Encję
        ClassType typeToUpdate = classTypeOpt.get();
        typeToUpdate.setName(form.getName());
        typeToUpdate.setDescription(form.getDescription());
        typeToUpdate.setDefaultDurationMinutes(form.getDefaultDurationMinutes());
        typeToUpdate.setDifficulty(form.getDifficulty());

        // 4. Zapis
        classTypeRepository.save(typeToUpdate);

        // 5. Przekierowanie
        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Dane typu zajęć zostały pomyślnie zaktualizowane.");
        return "redirect:/classtypes";
    }
}