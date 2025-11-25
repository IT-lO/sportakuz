package com.icio.sportakuz.repo;

/**
 * Status rezerwacji uczestnika na zajęcia.
 * REQUESTED - zgłoszona, oczekuje na potwierdzenie
 * CONFIRMED - potwierdzona (miejsce zarezerwowane)
 * PAID - opłacona (jeśli wymagane płatności)
 * CANCELLED - anulowana przez system lub użytkownika
 */
public enum BookingStatus {
    REQUESTED("Zgłoszona"),
    CONFIRMED("Potwierdzona"),
    PAID("Opłacona"),
    CANCELLED("Anulowana");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
