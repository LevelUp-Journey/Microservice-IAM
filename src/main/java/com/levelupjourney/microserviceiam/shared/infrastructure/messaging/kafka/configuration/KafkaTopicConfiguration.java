package com.levelupjourney.microserviceiam.shared.infrastructure.messaging.kafka.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Topic Configuration
 * Defines and creates Kafka topics for the IAM microservice
 *
 * @author LevelUp Journey Team
 */
@Configuration
public class KafkaTopicConfiguration {

    /**
     * Creates the user registered topic
     * This topic is used to publish events when a new user registers in the system
     *
     * @return NewTopic configuration
     */
    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name("iam.user.registered")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
