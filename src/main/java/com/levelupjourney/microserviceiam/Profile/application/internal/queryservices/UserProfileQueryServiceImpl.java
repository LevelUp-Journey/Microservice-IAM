package com.levelupjourney.microserviceiam.Profile.application.internal.queryservices;

import com.levelupjourney.microserviceiam.Profile.domain.model.aggregates.UserProfile;
import com.levelupjourney.microserviceiam.Profile.domain.model.queries.*;
import com.levelupjourney.microserviceiam.Profile.domain.services.UserProfileQueryService;
import com.levelupjourney.microserviceiam.Profile.infrastructure.persistence.jpa.repositories.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserProfileQueryServiceImpl implements UserProfileQueryService {
    
    private final UserProfileRepository userProfileRepository;
    
    public UserProfileQueryServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> handle(GetUserProfileByIdQuery query) {
        return userProfileRepository.findById(query.profileId().value());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> handle(GetUserProfileByAccountIdQuery query) {
        return userProfileRepository.findByAccountId(query.accountId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> handle(GetUserProfileByUsernameQuery query) {
        return userProfileRepository.findByUsername(query.username());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserProfile> handle(GetAllUserProfilesQuery query) {
        int page = query.page().orElse(0);
        int pageSize = query.pageSize().orElse(10);
        String searchQuery = query.searchQuery().orElse(null);
        
        Pageable pageable = PageRequest.of(page, pageSize);
        
        return userProfileRepository.findAllWithSearch(searchQuery, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserProfile> handle(GetUserProfilesByUsernameQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.pageSize());
        return userProfileRepository.findByUsernameContainingIgnoreCase(query.searchTerm(), pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserProfile> handle(GetUserProfilesByRoleQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.pageSize());
        return userProfileRepository.findByRole(query.role(), pageable);
    }
}