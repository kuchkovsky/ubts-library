package ua.org.ubts.library.service;

import org.springframework.security.core.Authentication;
import ua.org.ubts.library.entity.UserEntity;

import java.security.Principal;

public interface UserService {

    UserEntity getUser(Long id);

    UserEntity getUser(String login);

    UserEntity getUser(Principal principal);

    UserEntity getUser(Authentication authentication);

    void createUser(UserEntity userEntity);

    void updateUser(UserEntity userEntity);

    void updateUser(UserEntity userEntity, Principal principal);

    void deleteUser(Long id);

}
