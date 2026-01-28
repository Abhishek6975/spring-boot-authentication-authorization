package com.koyta.auth.controllers;

import com.koyta.auth.dtos.CreateUserRequest;
import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.entities.User;
import com.koyta.auth.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "User",description = "Authetication User Operation API's")
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    @PostMapping()
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setImage(request.getImage());
        user.setProvider(request.getProvider());

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email) {

        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @DeleteMapping("/{userid}")
    public void deleteUser(@PathVariable("userid") String userid){
        userService.deleteUser(userid);
    }

    @PutMapping("/{userid}")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto ,@PathVariable("userid") String userid){

        return ResponseEntity.ok(userService.updateUser(userDto,userid));
    }

    @GetMapping("/{userid}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userid") String userid){

        return ResponseEntity.ok(userService.getUserById(userid));
    }

}
