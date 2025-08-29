package com.levelupjourney.microserviceiam.IAM.infrastructure.hashing;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.hashing.HashingService;

@Service
public class BCryptHashingService implements HashingService {
    
    private final BCryptPasswordEncoder passwordEncoder;
    
    public BCryptHashingService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }
    
    @Override
    public String encode(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    @Override
    public boolean matches(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}