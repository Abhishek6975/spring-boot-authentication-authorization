package com.koyta.auth.services.impl;

import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.entities.Provider;
import com.koyta.auth.entities.User;
import com.koyta.auth.exceptions.ResourceNotFoundException;
import com.koyta.auth.helpers.UserHelper;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

 // @Autowired
    private final UserRepository userRepository;

    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

        if(userDto.getEmail() == null || userDto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is required");
        }

        if(userRepository.existsByEmail(userDto.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }

        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

        // role assign here to user for authorization

        User saveUser = userRepository.save(user);
        return modelMapper.map(saveUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID uid = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        // email id is Unique so Not Update

        if(userDto.getName() != null) user.setName(userDto.getName());
        if(userDto.getImage() != null) user.setImage(userDto.getImage());
        if(userDto.getProvider() != null) user.setProvider(userDto.getProvider());
        if(userDto.getPassword() != null) user.setPassword(userDto.getPassword());
        user.setEnable(userDto.isEnable());
        user.setUpdatedAt(Instant.now());
        User updateUser = userRepository.save(user);

        return modelMapper.map(updateUser , UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {

        UUID uid = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        userRepository.delete(user);

    }

    @Override
    public UserDto getUserById(String userId) {
        UUID uid = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    @Transactional
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}
