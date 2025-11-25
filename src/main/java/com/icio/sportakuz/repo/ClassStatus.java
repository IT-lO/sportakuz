package com.icio.sportakuz.repo;

/**
 * Status pojedynczego wystąpienia zajęć (ClassOccurrence) w cyklu życia.
 * PLANNED - utworzone, jeszcze nie otwarte do zapisów
 * OPEN - dostępne do rezerwacji
 * CANCELLED - odwołane
 * FINISHED - zakończone (archiwalne)
 */
public enum ClassStatus {
    PLANNED("Zaplanowane"),
    OPEN("Otwarte"),
    CANCELLED("Odwołane"),
    FINISHED("Zakończone");

    private final String label;

    ClassStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

