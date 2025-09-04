package com.levelupjourney.microserviceiam.shared.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

/**
 * Abstract class for aggregate roots that need auditing capabilities.
 * It extends AbstractAggregateRoot to support domain events.
 *
 * @param <T> the type of the aggregate root
 */
@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class AuditableAbstractAggregateRoot<T extends AbstractAggregateRoot<T>> extends AbstractAggregateRoot<T> {
    @Id
    @UuidGenerator
    private UUID id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;

    /**
     * Registers a domain event to be published.
     *
     * @param event the domain event to register
     */
    public void addDomainEvent(Object event) {
        registerEvent(event);
    }
}
