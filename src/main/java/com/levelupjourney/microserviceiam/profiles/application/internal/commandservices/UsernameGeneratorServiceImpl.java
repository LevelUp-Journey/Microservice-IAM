package com.levelupjourney.microserviceiam.profiles.application.internal.commandservices;

import com.levelupjourney.microserviceiam.profiles.domain.services.UsernameGeneratorService;
import com.levelupjourney.microserviceiam.profiles.infrastructure.persistence.jpa.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UsernameGeneratorServiceImpl implements UsernameGeneratorService {
    private final ProfileRepository profileRepository;
    private final SecureRandom random;
    private static final String USERNAME_PREFIX = "USER";
    private static final int DIGITS_COUNT = 9;

    public UsernameGeneratorServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        this.random = new SecureRandom();
    }

    @Override
    public String generateUniqueUsername() {
        String username;
        do {
            username = generateRandomUsername();
        } while (profileRepository.existsByUsernameUsername(username));
        
        return username;
    }

    private String generateRandomUsername() {
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < DIGITS_COUNT; i++) {
            digits.append(random.nextInt(10));
        }
        return USERNAME_PREFIX + digits.toString();
    }
}