package com.sebastianaldi17.walletapi.services;

import com.sebastianaldi17.walletapi.repositories.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class AdminService {
    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
