package com.icio.sportakuz.classes.domain;

/**
 * Status pojedynczego wystąpienia zajęć (ClassOccurrence) w cyklu życia.
 * PLANNED - utworzone, jeszcze nie otwarte do zapisów
 * OPEN - dostępne do rezerwacji
 * CANCELLED - odwołane
 * FINISHED - zakończone (archiwalne)
 */
public enum ClassStatus {PLANNED, OPEN, CANCELLED, FINISHED}
