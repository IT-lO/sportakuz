package com.icio.sportakuz.controller.security;

import com.icio.sportakuz.config.security.RecaptchaService;
import com.icio.sportakuz.dto.UserRegister;
import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.entity.UserRole;
import com.icio.sportakuz.repo.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecaptchaService recaptchaService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegister());
        return "login/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegister userDto,
                               BindingResult bindingResult,
                               @RequestParam(name="g-recaptcha-response") String captchaResponse,
                               Model model) {

        if (bindingResult.hasErrors()) {
            return "login/register";
        }

        boolean isCaptchaValid = recaptchaService.verify(captchaResponse);
        if (!isCaptchaValid) {
            model.addAttribute("error", "Potwierdź, że nie jesteś robotem!");
            return "login/register";
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            model.addAttribute("error", "Ten email jest już zajęty!");
            return "login/register";
        }

        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        user.setActive(true);

        userRepository.save(user);

        return "redirect:/login?success";
    }
}