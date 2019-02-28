package ua.org.ubts.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UbtsLibrary {

    public static void main(String[] args) {
        SpringApplication.run(UbtsLibrary.class, args);
    }

}
