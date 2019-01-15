package ua.org.ubts.library.config;

import io.jsonwebtoken.impl.crypto.MacProvider;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class AppConfig {

    private static final String INIT_DIRECTORIES_ERROR_MESSAGE = "Could not create application directories";

    @Bean
    public String appDirectory() {
        return System.getProperty("user.home") + File.separator + "ubts-library";
    }

    @Bean
    public String tmpDirectory() {
        return appDirectory() + File.separator + "tmp";
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey secretKey() {
        return MacProvider.generateKey();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    @PostConstruct
    public void initDirectories() {
        try {
            Files.createDirectories(Paths.get(appDirectory()));
            Files.createDirectories(Paths.get(tmpDirectory()));
        } catch (IOException e) {
            log.error(INIT_DIRECTORIES_ERROR_MESSAGE, e);
        }
    }

}
