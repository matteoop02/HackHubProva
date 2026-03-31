package unicam.ids.HackHub;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition
@EnableScheduling
@SpringBootApplication(scanBasePackages = "unicam.ids.HackHub")
public class Main {
    public static void main(String[] args) {

        SpringApplication.run(Main.class, args);
    }
}
