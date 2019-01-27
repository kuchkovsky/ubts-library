package ua.org.ubts.library.service;

import org.springframework.security.core.Authentication;
import ua.org.ubts.library.entity.UserEntity;

import java.security.Principal;
import java.util.List;

public interface UserService {

    UserEntity getUser(Long id);

    UserEntity getUser(String login);

    UserEntity getUser(Principal principal);

    UserEntity getUser(Authentication authentication);

    List<UserEntity> getMoodleUsers();

    void createUser(UserEntity userEntity);

    void updateUser(UserEntity userEntity);

    void updateUser(UserEntity userEntity, Principal principal);

    void deleteUser(Long id);

}
