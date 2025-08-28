package com.levelupjourney.microserviceiam.IAM.application.internal.services;

import com.levelupjourney.microserviceiam.IAM.domain.model.aggregates.User;
import com.levelupjourney.microserviceiam.IAM.domain.model.entities.UserAudit;
import com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories.UserAuditRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserAuditService {

    private final UserAuditRepository userAuditRepository;

    public UserAuditService(UserAuditRepository userAuditRepository) {
        this.userAuditRepository = userAuditRepository;
    }

    /**
     * Logs a field change for a user
     * @param user the user whose field was changed
     * @param changedByUser the user who made the change
     * @param fieldName the name of the field that was changed
     * @param oldValue the old value of the field
     * @param newValue the new value of the field
     */
    public void logFieldChange(User user, User changedByUser, String fieldName, String oldValue, String newValue) {
        if (isValueChanged(oldValue, newValue)) {
            UserAudit audit = new UserAudit(user, changedByUser, fieldName, oldValue, newValue);
            userAuditRepository.save(audit);
        }
    }

    /**
     * Logs a field change for a user with a reason
     * @param user the user whose field was changed
     * @param changedByUser the user who made the change
     * @param fieldName the name of the field that was changed
     * @param oldValue the old value of the field
     * @param newValue the new value of the field
     * @param reason the reason for the change
     */
    public void logFieldChange(User user, User changedByUser, String fieldName, String oldValue, String newValue, String reason) {
        if (isValueChanged(oldValue, newValue)) {
            UserAudit audit = new UserAudit(user, changedByUser, fieldName, oldValue, newValue, reason);
            userAuditRepository.save(audit);
        }
    }

    /**
     * Gets all audit records for a user
     * @param userId the user ID
     * @return list of audit records
     */
    public List<UserAudit> getUserAuditHistory(UUID userId) {
        return userAuditRepository.findByUserIdOrderByChangeTimestampDesc(userId);
    }

    /**
     * Gets audit records for a specific field of a user
     * @param userId the user ID
     * @param fieldName the field name
     * @return list of audit records for the field
     */
    public List<UserAudit> getUserFieldAuditHistory(UUID userId, String fieldName) {
        return userAuditRepository.findByUserIdAndFieldNameOrderByChangeTimestampDesc(userId, fieldName);
    }

    /**
     * Checks if a value has actually changed
     * @param oldValue the old value
     * @param newValue the new value
     * @return true if the value has changed, false otherwise
     */
    private boolean isValueChanged(String oldValue, String newValue) {
        if (oldValue == null && newValue == null) {
            return false;
        }
        if (oldValue == null || newValue == null) {
            return true;
        }
        return !oldValue.equals(newValue);
    }
}