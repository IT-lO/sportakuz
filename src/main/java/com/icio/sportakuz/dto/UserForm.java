package com.icio.sportakuz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) reprezentujący dane z formularza dodawania/edycji instruktora.
 * Zawiera walidację pól.
 */
@Getter
@Setter
public class UserForm {
    @NotBlank(message = "Imię jest wymagane.")
    @Size(max = 100, message = "Imię nie może przekraczać 100 znaków.")
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane.")
    @Size(max = 100, message = "Nazwisko nie może przekraczać 100 znaków.")
    private String lastName;

    @NotBlank(message = "Adres e-mail jest wymagany.")
    @Email(message = "Wprowadź poprawny adres e-mail.")
    @Size(max = 255, message = "Adres e-mail nie może przekraczać 255 znaków.")
    private String email;

    // --- NOWE POLE ---
    // Nie dajemy tu @NotBlank, aby umożliwić edycję użytkownika bez zmiany hasła.
    // Walidacja "czy hasło jest wpisane przy tworzeniu" odbywa się w kontrolerze (processCreateForm).
    @Size(min = 4, max = 100, message = "Hasło powinno mieć od 4 do 100 znaków.")
    private String password;
    // -----------------

    @Size(max = 40, message = "Numer telefonu nie może przekraczać 40 znaków.")
    private String phone;

    @Size(max = 2000, message = "Opis 'bio' nie może przekraczać 2000 znaków.")
    private String bio;

    private boolean active = true;
}