package com.icio.sportakuz.repo;

/**
 * Określa częstotliwość powtarzania serii zajęć.
 * Używany w klasie ClassSeries do generowania kolejnych wystąpień.
 */
public enum RecurrencePattern {
    /** Zajęcia odbywają się każdego dnia. */
    DAILY("Codziennie"),

    /** Zajęcia odbywają się co tydzień (np. w tę samą godzinę i dzień). */
    WEEKLY("Co tydzień"),

    /** Zajęcia odbywają się co miesiąc (np. tego samego dnia miesiąca). */
    MONTHLY("Co miesiąc");

    private final String label;

    RecurrencePattern(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

