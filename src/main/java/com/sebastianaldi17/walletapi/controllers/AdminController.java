package com.sebastianaldi17.walletapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
    @GetMapping(path = "/admin")
    public ResponseEntity<String> pingAdmin() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
