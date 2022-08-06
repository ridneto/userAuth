package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDTO;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(
            SpecificationTemplate.UserSpec spec,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "userId",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ){
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll(spec, pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable(value = "userId") UUID userId){
        Optional<UserModel> userModelOptional = userService.findById(userId);

        if(!userModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value="userId") UUID userId){
        Optional<UserModel> userModelOptional = userService.findById(userId);

        if(userModelOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        userService.delete(userModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Object> updateUser(
            @PathVariable(value="userId")
            UUID userId,

            @RequestBody
            @Validated(UserDTO.UserView.UserPut.class)
            @JsonView(UserDTO.UserView.UserPut.class)
            UserDTO userDto
    ){
        Optional<UserModel> userModelOptional = userService.findById(userId);

        if(userModelOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        var userModel = userModelOptional.get();

        userModel.setFullName(userDto.getFullName());
        userModel.setPhoneNumber(userDto.getPhoneNumber());
        userModel.setCpf(userDto.getCpf());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        userService.save(userModel);

        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<Object> updatePassword(
            @PathVariable(value="userId")
            UUID userId,

            @RequestBody
            @Validated(UserDTO.UserView.PasswordPut.class)
            @JsonView(UserDTO.UserView.PasswordPut.class)
            UserDTO userDto
    ){
        Optional<UserModel> userModelOptional = userService.findById(userId);

        if(userModelOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if(!userModelOptional.get().getPassword().equals(userDto.getOldPassword())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid credentials");
        }

        var userModel = userModelOptional.get();

        userModel.setPassword(userDto.getPassword());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        userService.save(userModel);

        return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully");
    }

    @PatchMapping("/{userId}/image")
    public ResponseEntity<Object> updateImage(
            @PathVariable(value="userId")
            UUID userId,

            @RequestBody
            @Validated(UserDTO.UserView.ImagePut.class)
            @JsonView(UserDTO.UserView.ImagePut.class)
            UserDTO userDto
    ){
        Optional<UserModel> userModelOptional = userService.findById(userId);

        if(userModelOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        var userModel = userModelOptional.get();

        userModel.setImageUrl(userDto.getImageUrl());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        userService.save(userModel);

        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }
}
