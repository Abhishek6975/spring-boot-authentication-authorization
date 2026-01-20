package com.koyta.auth.controllers;

import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "User",description = "Authetication User Operation API's")
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping()
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
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
