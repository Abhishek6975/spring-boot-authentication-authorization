package com.koyta.auth.security;

import com.koyta.auth.repositories.RoleRepository;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl
        implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        return userRepository.findByEmail(username)
                .switchIfEmpty(Mono.error(
                        new UsernameNotFoundException("User not found")
                ))
                .flatMap(user ->
                        userRoleRepository.findByUserId(user.getId())
                                .flatMap(ur -> roleRepository.findById(ur.getRoleId()))
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                                .collectList()
                                .map(authorities ->
                                        User.withUsername(user.getEmail())
                                                .password(user.getPassword())
                                                .disabled(!user.isEnable())
                                                .authorities(authorities)
                                                .build()
                                )
                );
    }
}

