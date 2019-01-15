package ua.org.ubts.library.service.impl;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ua.org.ubts.library.dto.TokenDto;
import ua.org.ubts.library.entity.UserEntity;
import ua.org.ubts.library.service.SecuredFileTokenService;
import ua.org.ubts.library.service.UserService;

import javax.crypto.SecretKey;
import javax.transaction.Transactional;
import java.util.Date;

@Service
@Transactional
public class SecuredFileTokenServiceImpl implements SecuredFileTokenService {

    private static final long TOKEN_EXPIRATION_TIME = 2000; // 2s

    @Autowired
    private UserService userService;

    @Autowired
    private SecretKey secretKey;

    @Override
    public TokenDto getDownloadToken(Authentication authentication) {
        UserEntity userEntity = userService.getUser(authentication);
        Claims claims = Jwts.claims().setSubject(String.valueOf(userEntity.getId()));
        String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
        return new TokenDto(token);
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        } catch (JwtException | NumberFormatException e) {
            return false;
        }
        return true;
    }

}
