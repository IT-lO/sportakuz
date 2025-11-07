package com.icio.sportakuz.classes.domain;

/**
 * Określa częstotliwość powtarzania serii zajęć.
 * Używany w klasie ClassSeries do generowania kolejnych wystąpień.
 */
public enum RecurrencePattern {
    /** Zajęcia odbywają się każdego dnia. */
    DAILY,
    /** Zajęcia odbywają się co tydzień (np. w tę samą godzinę i dzień). */
    WEEKLY,
    /** Zajęcia odbywają się co miesiąc (np. tego samego dnia miesiąca). */
    MONTHLY
}
