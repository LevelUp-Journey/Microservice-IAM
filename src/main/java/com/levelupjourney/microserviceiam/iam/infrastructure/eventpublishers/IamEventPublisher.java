package com.levelupjourney.microserviceiam.iam.infrastructure.eventpublishers;

import com.levelupjourney.microserviceiam.iam.domain.model.events.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * IAM Event Publisher
 * Publishes domain events from IAM to Kafka topics
 * Falls back gracefully if Kafka is not available
 *
 * @author LevelUp Journey Team
 */
@Service
public class IamEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(IamEventPublisher.class);
    private static final String USER_REGISTERED_TOPIC = "iam.user.registered";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
    
    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled;

    public IamEventPublisher(KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a user registered event to Kafka
     * Uses fire-and-forget approach with error handling to avoid blocking the API
     *
     * @param event The user registered event
     */
    public void publishUserRegistered(UserRegisteredEvent event) {
        // Skip Kafka if disabled
        if (!kafkaEnabled) {
            logger.debug("Kafka is disabled, skipping event publication for userId: {}", event.getUserId());
            return;
        }

        String key = event.getUserId().toString();
        logger.info("Publishing UserRegisteredEvent for userId: {} to topic: {}", key, USER_REGISTERED_TOPIC);

        try {
            // Fire and forget - no bloqueamos el endpoint
            CompletableFuture<SendResult<String, UserRegisteredEvent>> future = 
                kafkaTemplate.send(USER_REGISTERED_TOPIC, key, event);

            // Manejo asíncrono del resultado - no bloquea
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Failed to publish UserRegisteredEvent for userId: {}. Error: {}", 
                        key, ex.getMessage());
                    logger.debug("Full error stack trace:", ex);
                } else {
                    logger.info("Successfully published UserRegisteredEvent for userId: {} to partition: {}, offset: {}",
                            key,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception ex) {
            // Captura excepciones síncronas (ej: Kafka no disponible)
            logger.error("Exception while sending to Kafka for userId: {}. API continues normally. Error: {}", 
                key, ex.getMessage());
            logger.debug("Full error stack trace:", ex);
            // No lanzamos la excepción - el endpoint debe continuar
        }
    }
}
