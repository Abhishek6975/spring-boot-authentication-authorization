package com.koyta.auth.service;

import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.entities.Provider;
import com.koyta.auth.entities.Role;
import com.koyta.auth.entities.User;
import com.koyta.auth.exceptions.ResourceNotFoundException;
import com.koyta.auth.repositories.RoleRepository;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.services.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private ModelMapper modelMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldThrowWhenEmailBlank() {

        User user = new User();
        user.setEmail("   "); // 🔥 blank case

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(user));
    }

    @Test
    void shouldThrowWhenUserNotFoundByEmail() {

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByEmail("test@test.com"));
    }

    @Test
    void shouldUpdateOnlyEnableWhenAllFieldsNull() {

        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any()))
                .thenReturn(user);

        UserDto dto = new UserDto(); // all null
        dto.setEnable(false);

        userService.updateUser(dto, id.toString());

        assertFalse(user.isEnable()); // 🔥 branch hit
    }

    @Test
    void shouldUpdateOnlyName() {

        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any()))
                .thenReturn(user);

        UserDto dto = new UserDto();
        dto.setName("Abhi");

        userService.updateUser(dto, id.toString());

        assertEquals("Abhi", user.getName());
    }

    @Test
    void shouldThrowWhenDeleteUserNotFound() {

        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(UUID.randomUUID().toString()));
    }

    @Test
    void shouldThrowWhenUserNotFoundById() {

        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(UUID.randomUUID().toString()));
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {

        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenEmailIsBlankOnly() {

        User user = new User();
        user.setEmail(""); // 🔥 empty string (NOT spaces)

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(user));
    }

    @Test
    void shouldUseExistingRoleWithoutSaving() {

        User user = new User();
        user.setEmail("test@test.com");

        Role role = new Role(UUID.randomUUID(), "USER");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(any())).thenReturn(Optional.of(role));
        when(userRepository.save(any())).thenReturn(user);

        userService.createUser(user);

        verify(roleRepository, never()).save(any()); // 🔥 critical
    }

    @Test
    void shouldCreateRoleWhenNotExistsBranch() {

        User user = new User();
        user.setEmail("test@test.com");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(any())).thenReturn(Optional.empty());

        Role role = new Role(UUID.randomUUID(), "USER");

        when(roleRepository.save(any())).thenReturn(role);
        when(userRepository.save(any())).thenReturn(user);

        userService.createUser(user);

        verify(roleRepository).save(any()); // 🔥 second branch
    }

    @Test
    void shouldReturnUserWhenEmailExists() {

        User user = new User();
        user.setEmail("test@test.com");

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(user));

        when(modelMapper.map(any(), eq(UserDto.class)))
                .thenReturn(new UserDto());

        UserDto dto = userService.getUserByEmail("test@test.com");

        assertNotNull(dto);
    }

    @Test
    void shouldThrowWhenInvalidUUID() {

        assertThrows(Exception.class, () ->
                userService.getUserById("invalid-uuid"));
    }

    @Test
    void shouldUpdateProvider() {

        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserDto dto = new UserDto();
        dto.setProvider(Provider.LOCAL); // 🔥 missing

        userService.updateUser(dto, id.toString());

        assertEquals(Provider.LOCAL, user.getProvider());
    }

    @Test
    void shouldUpdateImage() {

        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserDto dto = new UserDto();
        dto.setImage("img.png");

        userService.updateUser(dto, id.toString());

        assertEquals("img.png", user.getImage());
    }

    @Test
    void shouldUpdatePassword() {

        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserDto dto = new UserDto();
        dto.setPassword("newpass");

        userService.updateUser(dto, id.toString());

        assertEquals("newpass", user.getPassword());
    }

    @Test
    void shouldThrowWhenEmailNullOrBlank() {

        User user = new User();
        user.setEmail(null);

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(user));

        user.setEmail("");

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(user));
    }

    @Test
    void shouldUpdateProviderWhenPresent() {

        UUID id = UUID.randomUUID();

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setEmail("test@test.com");

        when(userRepository.findById(eq(id)))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(modelMapper.map(any(), eq(UserDto.class)))
                .thenReturn(new UserDto());

        UserDto dto = new UserDto();
        dto.setProvider(Provider.GOOGLE);

        userService.updateUser(dto, id.toString());

        assertEquals(Provider.GOOGLE, existingUser.getProvider());

        verify(userRepository).save(existingUser);
    }

    @Test
    void shouldSkipProviderWhenNull() {

        UUID id = UUID.randomUUID();

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setProvider(Provider.LOCAL);

        when(userRepository.findById(eq(id)))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(any()))
                .thenReturn(existingUser);

        when(modelMapper.map(any(), eq(UserDto.class)))
                .thenReturn(new UserDto());

        UserDto dto = new UserDto();
        dto.setProvider(null);

        userService.updateUser(dto, id.toString());

        assertEquals(Provider.LOCAL, existingUser.getProvider());
    }

    @Test
    void shouldReturnAllUsersMapped() {

        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setEmail("a@test.com");

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setEmail("b@test.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        when(modelMapper.map(any(User.class), eq(UserDto.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    UserDto dto = new UserDto();
                    dto.setEmail(u.getEmail());
                    return dto;
                });

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("a@test.com", result.get(0).getEmail());
    }

}
