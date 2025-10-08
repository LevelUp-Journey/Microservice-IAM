package com.levelupjourney.microserviceiam.iam.infrastructure.eventpublishers;

import com.levelupjourney.microserviceiam.iam.domain.model.events.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * IAM Event Publisher
 * Publishes domain events from IAM to Kafka topics
 *
 * @author LevelUp Journey Team
 */
@Service
public class IamEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(IamEventPublisher.class);
    private static final String USER_REGISTERED_TOPIC = "iam.user.registered";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public IamEventPublisher(KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a user registered event to Kafka
     *
     * @param event The user registered event
     */
    public void publishUserRegistered(UserRegisteredEvent event) {
        String key = event.getUserId().toString();

        logger.info("Publishing UserRegisteredEvent for userId: {} to topic: {}", key, USER_REGISTERED_TOPIC);

        CompletableFuture<SendResult<String, UserRegisteredEvent>> future =
            kafkaTemplate.send(USER_REGISTERED_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                logger.error("Failed to publish UserRegisteredEvent for userId: {}", key, ex);
            } else {
                logger.info("Successfully published UserRegisteredEvent for userId: {} to partition: {}, offset: {}",
                        key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
