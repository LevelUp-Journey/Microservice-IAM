package com.levelupjourney.microserviceiam.IAM.application.internal.outboundservices.hashing;

public interface HashingService {
    String encode(String plainPassword);
    boolean matches(String plainPassword, String hashedPassword);
}