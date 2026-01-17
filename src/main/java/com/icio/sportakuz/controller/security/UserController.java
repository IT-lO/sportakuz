package com.icio.sportakuz.controller.security;

import com.icio.sportakuz.dto.UserForm;
import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.entity.UserRole;
import com.icio.sportakuz.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // POTRZEBNE DO HASŁA

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showInstructorList(Model model) {
        List<User> instructors = userRepository.findByRole(UserRole.ROLE_INSTRUCTOR);
        model.addAttribute("instructors", instructors);
        model.addAttribute("pageTitle", "Instruktorzy");
        return "users/list_instructors";
    }

    @GetMapping("/newInstructor")
    public String showCreateForm(Model model) {
        UserForm form = new UserForm();
        form.setActive(true);
        model.addAttribute("instructorForm", form);
        model.addAttribute("pageTitle", "Dodaj nowego instruktora");
        return "users/form_add_instructor";
    }

    @PostMapping("/newInstructor")
    public String processCreateForm(@Valid @ModelAttribute("instructorForm") UserForm form,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        // 1. Walidacja ręczna: Hasło jest wymagane przy tworzeniu nowego użytkownika
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "password.required", "Hasło jest wymagane dla nowego konta.");
        }

        // 2. Unikalność e-maila
        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "email.duplicate", "Instruktor o tym adresie e-mail już istnieje.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Dodaj nowego instruktora");
            return "users/form_add_instructor";
        }

        // 3. Mapowanie
        User newInstructor = new User();
        newInstructor.setFirstName(form.getFirstName());
        newInstructor.setLastName(form.getLastName());
        newInstructor.setEmail(form.getEmail());

        // --- KODOWANIE HASŁA ---
        newInstructor.setPassword(passwordEncoder.encode(form.getPassword()));
        // -----------------------

        newInstructor.setPhone(form.getPhone());
        newInstructor.setBio(form.getBio());
        newInstructor.setActive(form.isActive());
        newInstructor.setRole(UserRole.ROLE_INSTRUCTOR);

        userRepository.save(newInstructor);

        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Instruktor został pomyślnie dodany.");
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> instructorOpt = userRepository.findById(id);

        if (instructorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie znaleziono instruktora o ID: " + id);
            return "redirect:/users"; // Poprawiono przekierowanie na /users
        }

        User instructor = instructorOpt.get();
        UserForm form = new UserForm();
        form.setFirstName(instructor.getFirstName());
        form.setLastName(instructor.getLastName());
        form.setEmail(instructor.getEmail());
        form.setPhone(instructor.getPhone());
        form.setBio(instructor.getBio());
        form.setActive(instructor.isActive());
        // Hasła nie ustawiamy w formularzu ze względów bezpieczeństwa (pole pozostaje puste)

        model.addAttribute("instructorForm", form);
        model.addAttribute("instructorId", id);
        model.addAttribute("pageTitle", "Edytuj instruktora: " + instructor.getFirstName() + " " + instructor.getLastName());
        return "users/form_edit_instructor";
    }

    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable("id") Long id,
                                  @Valid @ModelAttribute("instructorForm") UserForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        Optional<User> instructorOpt = userRepository.findById(id);
        if (instructorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Błąd edycji: Nie znaleziono instruktora.");
            return "redirect:/users";
        }

        User existingUser = instructorOpt.get();

        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            // Sprawdzamy czy email należy do kogoś innego niż edytowany user
            Optional<User> otherUser = userRepository.findByEmail(form.getEmail());
            if (otherUser.isPresent() && !otherUser.get().getId().equals(id)) {
                bindingResult.rejectValue("email", "email.duplicate", "Inny użytkownik ma ten e-mail.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edytuj instruktora");
            model.addAttribute("instructorId", id);
            return "users/form_edit_instructor";
        }

        existingUser.setFirstName(form.getFirstName());
        existingUser.setLastName(form.getLastName());
        existingUser.setEmail(form.getEmail());
        existingUser.setPhone(form.getPhone());
        existingUser.setBio(form.getBio());
        existingUser.setActive(form.isActive());

        // --- ZMIANA HASŁA (OPCJONALNA PRZY EDYCJI) ---
        // Jeśli pole w formularzu nie jest puste, zmieniamy hasło.
        // Jeśli jest puste, zostawiamy stare.
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        // ---------------------------------------------

        userRepository.save(existingUser);

        redirectAttributes.addFlashAttribute("globalSuccessMessage", "Zaktualizowano dane instruktora.");
        return "redirect:/users";
    }

    @PostMapping("/delete/{id}")
    public String processDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("globalSuccessMessage", "Usunięto instruktora.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Nie można usunąć instruktora, który ma przypisane zajęcia.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("globalErrorMessage", "Błąd usuwania.");
        }
        return "redirect:/users";
    }
}