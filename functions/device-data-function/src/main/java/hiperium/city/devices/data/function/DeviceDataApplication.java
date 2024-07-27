package hiperium.city.devices.data.function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The DeviceDataApplication class is the entry point for running the application.
 * It is annotated with @SpringBootApplication, indicating that it is a Spring Boot application.
 */
@SpringBootApplication
public class DeviceDataApplication {

    /**
     * The main method of the DeviceDataApplication class.
     * It is the entry point for running the application.
     *
     * @param args an array of command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(DeviceDataApplication.class, args);
    }
}
