package com.levelupjourney.microserviceiam.iam.domain.model.valueobjects;

public record OAuth2UserInfo(
        String firstName,
        String lastName, 
        String profileUrl,
        String email
) {
}