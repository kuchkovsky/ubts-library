package ua.org.ubts.library.service;

import org.springframework.security.core.Authentication;
import ua.org.ubts.library.dto.TokenDto;

public interface SecuredFileTokenService {

    TokenDto getDownloadToken(Authentication authentication);

    boolean verifyToken(String token);

}
