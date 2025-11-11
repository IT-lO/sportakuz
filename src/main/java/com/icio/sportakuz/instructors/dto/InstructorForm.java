package com.icio.sportakuz.instructors.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) reprezentujący dane z formularza dodawania/edycji instruktora.
 * Zawiera walidację pól.
 */
public class InstructorForm {

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

    @Size(max = 40, message = "Numer telefonu nie może przekraczać 40 znaków.")
    private String phone;

    @Size(max = 2000, message = "Opis 'bio' nie może przekraczać 2000 znaków.")
    private String bio;

    private boolean active = true;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}