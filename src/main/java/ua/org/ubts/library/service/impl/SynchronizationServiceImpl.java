package ua.org.ubts.library.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.org.ubts.library.dto.MoodleUserList;
import ua.org.ubts.library.entity.UserEntity;
import ua.org.ubts.library.service.SynchronizationService;
import ua.org.ubts.library.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@Slf4j
public class SynchronizationServiceImpl implements SynchronizationService {

    private static final String MOODLE_USERS_SYNCHRONIZATION_URL_TEMPLATE = "%s/webservice/rest/server.php?wstoken=%s"
            + "&wsfunction=core_user_get_users&criteria[0][key]=email&criteria[0][value]=%%&moodlewsrestformat=json";

    @Value("${UBTS_LIBRARY_MOODLE_HOST}")
    private String moodleHost;

    @Value("${UBTS_LIBRARY_MOODLE_TOKEN}")
    private String moodleToken;

    @Autowired
    private UserService userService;

    private String deriveMoodleUsersSynchronizationUrl() {
        return String.format(MOODLE_USERS_SYNCHRONIZATION_URL_TEMPLATE, moodleHost, moodleToken);
    }

    @Override
    public void synchronizeMoodleUsers() {
        log.info("Synchronizing Moodle users...");
        RestTemplate restTemplate = new RestTemplate();
        String url = deriveMoodleUsersSynchronizationUrl();
        MoodleUserList moodleUserList = restTemplate.getForObject(url, MoodleUserList.class);
        if (moodleUserList != null) {
            List<UserEntity> savedMoodleUsers = userService.getMoodleUsers();
            moodleUserList.getUsers().stream()
                    .filter(moodleUser -> savedMoodleUsers.stream()
                            .noneMatch(userEntity -> userEntity.getLogin().equals(moodleUser.getUsername())))
                    .forEach(moodleUser -> {
                        log.info("Adding user: {}", moodleUser.getUsername());
                        UserEntity userEntity = new UserEntity();
                        userEntity.setLogin(moodleUser.getUsername());
                        userEntity.setPassword("N/A");
                        userEntity.setFirstName(moodleUser.getFirstname());
                        userEntity.setLastName(moodleUser.getLastname());
                        userEntity.setMoodleUser(true);
                        userService.createUser(userEntity);
                    });
            userService.getMoodleUsers().stream()
                    .filter(userEntity -> moodleUserList.getUsers().stream()
                            .noneMatch(moodleUser -> moodleUser.getUsername().equals(userEntity.getLogin())))
                    .forEach(userEntity -> {
                        log.info("Deleting user: {}", userEntity.getLogin());
                        userService.deleteUser(userEntity.getId());
                    });
            log.info("Synchronization complete.");
        }
    }

}
