package com.icio.sportakuz.classtypes.controller;
import com.icio.sportakuz.classes.domain.ClassType;
import com.icio.sportakuz.classtypes.DifficultyLevel;
import com.icio.sportakuz.classes.repo.ClassTypeRepository;
import com.icio.sportakuz.classtypes.dto.ClassTypeForm;
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
@RequestMapping("/classtypes")
public class ClassTypeController {

    private final ClassTypeRepository classTypeRepository;

    public ClassTypeController(ClassTypeRepository classTypeRepository) {
        this.classTypeRepository = classTypeRepository;
    }

    @GetMapping
    public String showClassTypeList(Model model) {
        List<ClassType> classTypes = classTypeRepository.findAll(Sort.by("name"));
        model.addAttribute("classTypes", classTypes);
        model.addAttribute("pageTitle", "Typy Zajęć");
        return "classtypes/list_classtypes";
    }


    /**
     * Wyświetla formularz dodawania nowego typu zajęć.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("classTypeForm", new ClassTypeForm());
        // DODANE: Przekaż listę Enumów do widoku
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
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

        if (classTypeRepository.findByName(form.getName()).isPresent()) {
            bindingResult.rejectValue("name", "name.duplicate", "Typ zajęć o tej nazwie już istnieje.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("pageTitle", "Dodaj nowy typ zajęć");
            return "classtypes/form_add_classtype";
        }

        ClassType newType = new ClassType();
        newType.setName(form.getName());
        newType.setDescription(form.getDescription());
        newType.setDefaultDurationMinutes(form.getDefaultDurationMinutes());
        newType.setDifficulty(form.getDifficulty());

        classTypeRepository.save(newType);

        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Nowy typ zajęć został pomyślnie dodany.");
        return "redirect:/classtypes";
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

        ClassType classType = classTypeOpt.get();
        ClassTypeForm form = new ClassTypeForm();
        form.setName(classType.getName());
        form.setDescription(classType.getDescription());
        form.setDefaultDurationMinutes(classType.getDefaultDurationMinutes());
        form.setDifficulty(classType.getDifficulty());

        model.addAttribute("classTypeForm", form);
        model.addAttribute("classTypeId", id);
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        model.addAttribute("pageTitle", "Edytuj typ zajęć: " + classType.getName());
        return "classtypes/form_edit_classtype";
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

        Optional<ClassType> existingName = classTypeRepository.findByName(form.getName());
        if (existingName.isPresent() && !existingName.get().getId().equals(id)) {
            bindingResult.rejectValue("name", "name.duplicate", "Inny typ zajęć używa już tej nazwy.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("pageTitle", "Edytuj typ zajęć");
            model.addAttribute("classTypeId", id);
            return "classtypes/form_edit_classtype";
        }

        ClassType typeToUpdate = classTypeOpt.get();
        typeToUpdate.setName(form.getName());
        typeToUpdate.setDescription(form.getDescription());
        typeToUpdate.setDefaultDurationMinutes(form.getDefaultDurationMinutes());
        typeToUpdate.setDifficulty(form.getDifficulty());

        classTypeRepository.save(typeToUpdate);

        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Dane typu zajęć zostały pomyślnie zaktualizowane.");
        return "redirect:/classtypes";
    }

    /**
     * Przetwarza żądanie usunięcia typu zajęć.
     */
    @PostMapping("/delete/{id}")
    public String processDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        Optional<ClassType> classTypeOpt = classTypeRepository.findById(id);
        if (classTypeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie znaleziono typu zajęć do usunięcia (ID: " + id + ").");
            return "redirect:/classtypes";
        }

        try {
            classTypeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("globalSuccessMessage", "Typ zajęć został pomyślnie usunięty.");

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage",
                    "Nie można usunąć tego typu zajęć, ponieważ jest on powiązany z istniejącymi zajęciami. Najpierw usuń lub zmień powiązane zajęcia.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Wystąpił nieoczekiwany błąd podczas usuwania typu zajęć.");
        }

        return "redirect:/classtypes";
    }
}