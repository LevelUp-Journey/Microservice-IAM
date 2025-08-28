package com.levelupjourney.microserviceiam;

import java.io.Console;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
    "com.levelupjourney.microserviceiam.IAM.infrastructure.persistence.jpa.repositories",
    "com.levelupjourney.microserviceiam.Profile.infrastructure.persistence.jpa.repositories"
})
@EntityScan(basePackages = {
    "com.levelupjourney.microserviceiam.IAM.domain.model",
    "com.levelupjourney.microserviceiam.Profile.domain.model",
    "com.levelupjourney.microserviceiam.shared.domain.model"
})
@EnableJpaAuditing
public class MicroserviceIamApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroserviceIamApplication.class, args);
        
        Console console = System.console();
        if (console != null) {
            console.printf("Swagger UI is available at: http://localhost:8080/swagger-ui/index.html");
        } else {
            System.out.println("Swagger UI is available at: http://localhost:8080/swagger-ui/index.html");
        }
    }

}
