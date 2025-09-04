package com.levelupjourney.microserviceiam.iam.domain.model.queries;

/**
 * Get user by email_address query
 * <p>
 *     This class represents the query to get a user by its email_address.
 * </p>
 * @param email_address the email_address of the user
 */
public record GetUserByEmail_addressQuery(String email_address) {
}
