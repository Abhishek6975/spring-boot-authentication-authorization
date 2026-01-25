package com.koyta.auth.services.impl;

import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.entities.Provider;
import com.koyta.auth.entities.User;
import com.koyta.auth.exceptions.ResourceNotFoundException;
import com.koyta.auth.helpers.UserHelper;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;


    @Override
    public Mono<UserDto> createUser(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            return Mono.error(new IllegalArgumentException("Email is required"));
        }

        return userRepository.findByEmail(userDto.getEmail())
                .flatMap(existing ->
                        Mono.<UserDto>error(
                                        new IllegalArgumentException("Email already exists")
                                )
                )
                .switchIfEmpty(Mono.defer(() -> {

                    User user = modelMapper.map(userDto, User.class);
                    user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
                    user.setEnable(true);
                    user.setCreatedAt(Instant.now());
                    user.setUpdatedAt(Instant.now());

                    return userRepository.save(user)
                            .map(saved -> modelMapper.map(saved, UserDto.class));
                }));
    }

    @Override
    public Mono<UserDto> getUserById(String userId) {
        UUID uid = UserHelper.parseUUID(userId);

        return userRepository.findById(uid)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("User Not Found")))
                .map(user -> modelMapper.map(user, UserDto.class));
    }

    @Override
    public Mono<UserDto> updateUser(UserDto userDto, String userId) {
        UUID uid = UserHelper.parseUUID(userId);
        return userRepository.findById(uid)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("User Not Found")))
                .flatMap(user -> {

                    if (userDto.getName() != null) user.setName(userDto.getName());
                    if (userDto.getImage() != null) user.setImage(userDto.getImage());
                    if (userDto.getProvider() != null) user.setProvider(userDto.getProvider());
                    if (userDto.getPassword() != null) user.setPassword(userDto.getPassword());

                    user.setEnable(userDto.isEnable());
                    user.setUpdatedAt(Instant.now());

                    return userRepository.save(user);
                })
                .map(updated -> modelMapper.map(updated, UserDto.class));
    }

    @Override
    public Mono<Void> deleteUser(String userId) {
        UUID uid = UserHelper.parseUUID(userId);
        return userRepository.findById(uid)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User Not Found")))
                .flatMap(userRepository::delete);
    }

    @Override
    public Mono<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("User Not Found")))
                .map(user -> modelMapper.map(user, UserDto.class));
    }

    @Override
    public Flux<UserDto> getAllUsers() {
        return userRepository.findAll()
                .map(user -> modelMapper.map(user, UserDto.class));
    }
}
