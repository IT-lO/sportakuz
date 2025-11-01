package com.icio.sportakuz.classes.repo;

import com.icio.sportakuz.classes.domain.ClassOccurrence;
import com.icio.sportakuz.classes.domain.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ClassOccurrenceRepository extends JpaRepository<ClassOccurrence, Long> {

    List<ClassOccurrence> findByStatusAndStartTimeBetween(
            ClassStatus status, OffsetDateTime from, OffsetDateTime to);

    List<ClassOccurrence> findByInstructor_IdAndStartTimeAfterOrderByStartTimeAsc(
            Long instructorId, OffsetDateTime since);

    // wykrywanie kolizji w sali (przy dodawaniu/edycji)
    @Query("""
           select c from ClassOccurrence c
           where c.room.id = :roomId
             and c.status <> com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    List<ClassOccurrence> findOverlappingInRoom(@Param("roomId") Long roomId,
                                                @Param("start") OffsetDateTime start,
                                                @Param("end") OffsetDateTime end);

    // wykrywanie kolizji instruktora
    @Query("""
           select c from ClassOccurrence c
           where c.instructor.id = :instructorId
             and c.status <> com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    List<ClassOccurrence> findOverlappingForInstructor(@Param("instructorId") Long instructorId,
                                                       @Param("start") OffsetDateTime start,
                                                       @Param("end") OffsetDateTime end);

    List<ClassOccurrence> findAllByOrderByStartTimeAsc();

    @Query("""
           select count(c) from ClassOccurrence c
           where c.room.id = :roomId
             and c.status <> com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    long countOverlappingInRoom(@Param("roomId") Long roomId,
                                @Param("start") OffsetDateTime start,
                                @Param("end") OffsetDateTime end);

    @Query("""
           select count(c) from ClassOccurrence c
           where c.instructor.id = :instructorId
             and c.status <> com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    long countOverlappingForInstructor(@Param("instructorId") Long instructorId,
                                       @Param("start") OffsetDateTime start,
                                       @Param("end") OffsetDateTime end);
}
