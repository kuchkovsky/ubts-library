package ua.org.ubts.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.org.ubts.library.dto.TokenDto;
import ua.org.ubts.library.service.SecuredFileTokenService;

@RestController
@RequestMapping("/tokens/books")
public class SecureFileTokenController {

    @Autowired
    private SecuredFileTokenService securedFileTokenService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public TokenDto getDownloadToken(Authentication authentication) {
        return securedFileTokenService.getDownloadToken(authentication);
    }

}
