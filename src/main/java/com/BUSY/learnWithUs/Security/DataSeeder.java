package com.BUSY.learnWithUs.Security;

import com.BUSY.learnWithUs.Entity.User;
import com.BUSY.learnWithUs.Entity.UserRole;
import com.BUSY.learnWithUs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createInstructor();
        createLearner();
    }

    private void createInstructor() {

        String email = "instructor@learnwithus.com";

        if (userRepository.findByEmail(email).isEmpty()) {

            User instructor = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("Instructor@123"))
                    .role(UserRole.INSTRUCTOR)
                    .build();

            userRepository.save(instructor);
        }
    }

    private void createLearner() {

        String email = "learner@learnwithus.com";

        if (userRepository.findByEmail(email).isEmpty()) {

            User learner = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("Learner@123"))
                    .role(UserRole.LEARNER)
                    .build();

            userRepository.save(learner);
        }
    }
}