package ua.org.ubts.library.providers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ua.org.ubts.library.entity.UserEntity;
import ua.org.ubts.library.repository.UserRepository;

import java.net.URI;

@Component
public class MoodleAwareAuthenticationProvider extends DaoAuthenticationProvider {

    private static final String MOODLE_USER_AUTH_URL_TEMPLATE = "%s/login/index.php";
    private static final String MOODLE_USER_AUTH_SUCCESS_QUERY = "?testsession=";

    @Value("${UBTS_LIBRARY_MOODLE_HOST}")
    private String moodleHost;

    @Autowired
    private UserRepository userRepository;

    private String deriveMoodleUserAuthUrl() {
        return String.format(MOODLE_USER_AUTH_URL_TEMPLATE, moodleHost);
    }

    private String deriveMoodleUserSuccessAuthUrl() {
        return deriveMoodleUserAuthUrl() + MOODLE_USER_AUTH_SUCCESS_QUERY;
    }

    private boolean authenticateMoodleUser(Authentication authentication) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", authentication.getName());
        map.add("password", (String) authentication.getCredentials());

        RestTemplate restTemplate = new RestTemplate();
        String url = deriveMoodleUserAuthUrl();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        URI locationHeader = response.getHeaders().getLocation();
        String location = locationHeader != null ? locationHeader.toString(): null;
        return StringUtils.startsWith(location, deriveMoodleUserSuccessAuthUrl());
    }

    @Autowired
    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    @Autowired
    @Lazy
    @Override
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        super.setPasswordEncoder(passwordEncoder);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String login = authentication.getName();
        UserEntity userEntity = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(login));
        if (userEntity.isMoodleUser()) {
            if (authenticateMoodleUser(authentication)) {
                String password = (String) authentication.getCredentials();
                UserDetails user = getUserDetailsService().loadUserByUsername(login);
                return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
            }
            return null;
        }
        return super.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }

}
