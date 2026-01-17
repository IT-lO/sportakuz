package com.icio.sportakuz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegister {

    @NotEmpty(message = "Imię jest wymagane")
    private String firstName;

    @NotEmpty(message = "Nazwisko jest wymagane")
    private String lastName;

    @NotEmpty(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Podaj poprawny adres email (np. jan@przyklad.pl)"
    )
    private String email;

    @NotEmpty(message = "Hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć minimum 8 znaków")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_.*-]).{8,}$",
            message = "Hasło musi zawierać małą i wielką literę, cyfrę oraz znak specjalny"
    )
    private String password;
}