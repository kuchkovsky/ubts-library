package ua.org.ubts.library.service.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import ua.org.ubts.library.entity.RoleEntity;
import ua.org.ubts.library.entity.UserEntity;
import ua.org.ubts.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Primary
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(login));
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (RoleEntity roleEntity : userEntity.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(roleEntity.getName()));
        }
        return new User(userEntity.getLogin(), userEntity.getPassword(), authorities);
    }

}
