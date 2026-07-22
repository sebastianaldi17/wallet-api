package com.sebastianaldi17.walletapi.controllers;

import com.sebastianaldi17.walletapi.dtos.requests.CreateUserRequest;
import com.sebastianaldi17.walletapi.dtos.responses.CreateUserResponse;
import com.sebastianaldi17.walletapi.services.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/users")
    public CreateUserResponse createUser(
            @RequestBody @Valid
            CreateUserRequest request
    ) {
        return userService.createUser(request);
    }
}
