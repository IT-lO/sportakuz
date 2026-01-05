package com.icio.sportakuz.config;

import com.icio.sportakuz.entity.*;
import com.icio.sportakuz.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final RoomRepository roomRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        createUsers();
        createActivityTypes();
        createRooms();
        if (activityRepository.count() == 0) {
            createScheduledActivities();
        }
    }

    private void createUsers() {
        if (!userRepository.existsByEmail("admin@sportakuz.pl")) {
            User admin = new User();
            admin.setEmail("admin@sportakuz.pl");
            admin.setFirstName("Główny");
            admin.setLastName("Administrator");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println(">>> DATA_INIT: Utworzono ADMINA");
        }

        if (!userRepository.existsByEmail("user@sportakuz.pl")) {
            User user = new User();
            user.setEmail("user@sportakuz.pl");
            user.setFirstName("Jan");
            user.setLastName("Kowalski");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(UserRole.ROLE_USER);
            user.setActive(true);
            userRepository.save(user);
            System.out.println(">>> DATA_INIT: Utworzono USERA");
        }

        if (!userRepository.existsByEmail("trener@sportakuz.pl")) {
            User instructor = new User();
            instructor.setEmail("trener@sportakuz.pl");
            instructor.setFirstName("Anna");
            instructor.setLastName("Lewandowska");
            instructor.setBio("Trenerka z pasją i wieloletnim doświadczeniem.");
            instructor.setPassword(passwordEncoder.encode("trener123"));
            instructor.setRole(UserRole.ROLE_INSTRUCTOR);
            instructor.setActive(true);
            userRepository.save(instructor);
            System.out.println(">>> DATA_INIT: Utworzono INSTRUKTORA");
        }
    }
    private void createActivityTypes() {
        if (!activityTypeRepository.existsByActivityName("Crossfit")) {
            ActivityType t1 = new ActivityType();
            t1.setActivityName("Crossfit");
            t1.setDescription("Wysoka intensywność.");
            t1.setDuration(60);
            activityTypeRepository.save(t1);
        }

        if (!activityTypeRepository.existsByActivityName("Joga")) {
            ActivityType t2 = new ActivityType();
            t2.setActivityName("Joga");
            t2.setDescription("Rozciąganie i relaks.");
            t2.setDuration(90); //
            activityTypeRepository.save(t2);
        }

        System.out.println(">>> DATA_INIT: Sprawdzono/Utworzono TYPY ZAJĘĆ");
    }

    private void createRooms() {
        if (roomRepository.findByName("Sala Główna").isEmpty()) {
            Room r1 = new Room();
            r1.setName("Sala Główna");
            r1.setCapacity(30);
            roomRepository.save(r1);
        }

        if (roomRepository.findByName("Sala Rowerowa").isEmpty()) {
            Room r2 = new Room();
            r2.setName("Sala Rowerowa");
            r2.setCapacity(15);
            roomRepository.save(r2);
        }
        System.out.println(">>> DATA_INIT: Sprawdzono/Utworzono SALE");
    }

    private void createScheduledActivities() {
        User instructor = userRepository.findByEmail("trener@sportakuz.pl").orElse(null);
        ActivityType crossfit = activityTypeRepository.findByActivityName("Crossfit").orElse(null);
        ActivityType joga = activityTypeRepository.findByActivityName("Joga").orElse(null);
        Room salaGlowna = roomRepository.findByName("Sala Główna").orElse(null);

        if (instructor == null || crossfit == null || joga == null || salaGlowna == null) {
            System.out.println(">>> DATA_INIT: Brak danych (Instruktor/Typy/Sale). Pomijam tworzenie zajęć.");
            return;
        }

        Activity act1 = new Activity();
        act1.setType(joga);
        act1.setRoom(salaGlowna);
        act1.setInstructor(instructor);

        OffsetDateTime start1 = OffsetDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);
        act1.setStartTime(start1);

        act1.setDurationMinutes(90);

        act1.setCapacity(20);
        act1.setStatus(ClassStatus.OPEN);

        Activity act2 = new Activity();
        act2.setType(crossfit);
        act2.setRoom(salaGlowna);
        act2.setInstructor(instructor);

        OffsetDateTime start2 = OffsetDateTime.now().plusDays(2).withHour(7).withMinute(0).withSecond(0).withNano(0);
        act2.setStartTime(start2);

        act2.setDurationMinutes(60);

        act2.setCapacity(15);
        act2.setStatus(ClassStatus.PLANNED);

        activityRepository.saveAll(Arrays.asList(act1, act2));
        System.out.println(">>> DATA_INIT: Utworzono HARMONOGRAM ZAJĘĆ (Joga i Crossfit)");
    }
}