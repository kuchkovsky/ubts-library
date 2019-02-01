package ua.org.ubts.library.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ua.org.ubts.library.service.SynchronizationService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class AppConfig {

    private static final String INIT_DIRECTORIES_ERROR_MESSAGE = "Could not create application directories";

    @Autowired
    private SynchronizationService synchronizationService;

    @Bean
    public String appDirectory() {
        return System.getProperty("user.home") + File.separator + "ubts-library";
    }

    @Bean
    public String tmpDirectory() {
        return appDirectory() + File.separator + "tmp";
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

    @Scheduled(cron = "0 0 */12 * * *") // every 12 hours
    @EventListener(ApplicationReadyEvent.class) // run immediately after application start
    public void synchronizeMoodleUsers() {
        synchronizationService.synchronizeMoodleUsers();
    }

    @Bean
    @Primary
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("default_task_executor_thread");
        executor.initialize();
        return executor;
    }

}
