package com.levelupjourney.microserviceiam.IAM.domain.model.queries;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Get user by email query
 * <p>
 *     This class represents the query to get a user by its email.
 * </p>
 * @param email the email of the user
 */
public record GetUserByEmailQuery(@NotBlank @Email String email) {
}