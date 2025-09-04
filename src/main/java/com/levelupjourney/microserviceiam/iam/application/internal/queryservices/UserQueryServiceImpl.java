package com.levelupjourney.microserviceiam.iam.application.internal.queryservices;

import com.levelupjourney.microserviceiam.iam.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.iam.domain.model.queries.GetAllUsersQuery;
import com.levelupjourney.microserviceiam.iam.domain.model.queries.GetUserByIdQuery;
import com.levelupjourney.microserviceiam.iam.domain.model.queries.GetUserByEmail_addressQuery;
import com.levelupjourney.microserviceiam.iam.domain.services.UserQueryService;
import com.levelupjourney.microserviceiam.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link UserQueryService} interface.
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    /**
     * Constructor.
     *
     * @param userRepository {@link UserRepository} instance.
     */
    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method is used to handle {@link GetAllUsersQuery} query.
     * @param query {@link GetAllUsersQuery} instance.
     * @return {@link List} of {@link User} instances.
     * @see GetAllUsersQuery
     */
    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    /**
     * This method is used to handle {@link GetUserByIdQuery} query.
     * @param query {@link GetUserByIdQuery} instance.
     * @return {@link Optional} of {@link User} instance.
     * @see GetUserByIdQuery
     */
    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    /**
     * This method is used to handle {@link GetUserByEmail_addressQuery} query.
     * @param query {@link GetUserByEmail_addressQuery} instance.
     * @return {@link Optional} of {@link User} instance.
     * @see GetUserByEmail_addressQuery
     */
    @Override
    public Optional<User> handle(GetUserByEmail_addressQuery query) {
        return userRepository.findByEmail_address(query.email_address());
    }
}
