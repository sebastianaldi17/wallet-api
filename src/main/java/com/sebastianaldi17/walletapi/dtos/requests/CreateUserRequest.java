package com.sebastianaldi17.walletapi.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    String description;
}
