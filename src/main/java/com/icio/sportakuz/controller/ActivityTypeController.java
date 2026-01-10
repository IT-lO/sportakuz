package com.icio.sportakuz.controller;

import com.icio.sportakuz.dto.ActivityTypeForm;
import com.icio.sportakuz.entity.ActivityType;
import com.icio.sportakuz.repo.DifficultyLevel;
import com.icio.sportakuz.repo.ActivityTypeRepository;
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
@RequestMapping("/activitytypes")
public class ActivityTypeController {

    private final ActivityTypeRepository activityTypeRepository;

    public ActivityTypeController(ActivityTypeRepository activityTypeRepository) {
        this.activityTypeRepository = activityTypeRepository;
    }

    @GetMapping
    public String showActivityTypeList(Model model) {
        List<ActivityType> activityTypes = activityTypeRepository.findAll(Sort.by("activityName"));
        model.addAttribute("activitytypes", activityTypes);
        model.addAttribute("pageTitle", "Typy Zajęć");
        return "activitytypes/list_activitytypes";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("activityTypeForm", new ActivityTypeForm());

        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        model.addAttribute("pageTitle", "Dodaj nowy typ zajęć");
        return "activitytypes/form_add_activitytype";
    }

    @PostMapping("/new")
    public String processCreateForm(@Valid @ModelAttribute("activityTypeForm") ActivityTypeForm form,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        if (activityTypeRepository.findByActivityName(form.getActivityName()).isPresent()) {
            bindingResult.rejectValue("activityName", "name.duplicate", "Typ zajęć o tej nazwie już istnieje.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("pageTitle", "Dodaj nowy typ zajęć");
            return "activitytypes/form_add_activitytype";
        }

        ActivityType newActivity = ActivityType.builder()
                .activityName(form.getActivityName())
                .description(form.getDescription())
                .duration(form.getDuration())
                .difficulty(form.getDifficulty())
                .build();

        activityTypeRepository.save(newActivity);

        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Nowy typ zajęć został pomyślnie dodany.");
        return "redirect:/activitytypes";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ActivityType> classTypeOpt = activityTypeRepository.findById(id);

        if (classTypeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie znaleziono typu zajęć o ID: " + id);
            return "redirect:/activitytypes";
        }

        ActivityType classType = classTypeOpt.get();

        ActivityTypeForm form = new ActivityTypeForm() ;
        form.setActivityName(classType.getActivityName());
        form.setDescription(classType.getDescription());
        form.setDuration(classType.getDuration());
        form.setDifficulty(classType.getDifficulty());

        model.addAttribute("activityTypeForm", form);
        model.addAttribute("activityTypeId", id);
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        model.addAttribute("pageTitle", "Edytuj typ zajęć: " + classType.getActivityName());
        return "activitytypes/form_edit_activitytype";
    }

    // POPRAWKA: Zmiana Long id na Integer id
    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable("id") Integer id,
                                  @Valid @ModelAttribute("activityTypeForm") ActivityTypeForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        Optional<ActivityType> classTypeOpt = activityTypeRepository.findById(id);
        if (classTypeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Błąd edycji: Nie znaleziono typu zajęć.");
            return "redirect:/activitytypes";
        }

        Optional<ActivityType> existingName = activityTypeRepository.findByActivityName(form.getActivityName());

        // Sprawdzamy, czy nazwa jest zajęta przez INNY rekord niż ten edytowany
        if (existingName.isPresent() && !existingName.get().getId().equals(id)) {
            bindingResult.rejectValue("activityName", "name.duplicate", "Inny typ zajęć używa już tej nazwy.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("pageTitle", "Edytuj typ zajęć");
            model.addAttribute("activityTypeId", id);
            return "activitytypes/form_edit_activitytype";
        }

        // Aktualizacja Encji danymi z DTO
        ActivityType typeToUpdate = classTypeOpt.get();
        typeToUpdate.setActivityName(form.getActivityName());
        typeToUpdate.setDescription(form.getDescription());
        typeToUpdate.setDuration(form.getDuration());
        typeToUpdate.setDifficulty(form.getDifficulty());

        activityTypeRepository.save(typeToUpdate);

        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Dane typu zajęć zostały pomyślnie zaktualizowane.");
        return "redirect:/activitytypes";
    }

    @PostMapping("/delete/{id}")
    public String processDelete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

        if (!activityTypeRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie znaleziono typu zajęć do usunięcia (ID: " + id + ").");
            return "redirect:/activitytypes";
        }

        try {
            activityTypeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("globalSuccessMessage", "Typ zajęć został pomyślnie usunięty.");

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage",
                    "Nie można usunąć tego typu zajęć, ponieważ jest on powiązany z istniejącymi zajęciami.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Wystąpił nieoczekiwany błąd podczas usuwania typu zajęć.");
        }

        return "redirect:/activitytypes";
    }
}