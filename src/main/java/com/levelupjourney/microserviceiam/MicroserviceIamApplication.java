package com.levelupjourney.microserviceiam;

import java.io.Console;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
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
