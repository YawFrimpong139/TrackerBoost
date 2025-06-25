package org.codewithzea.trackerboost.user.dto;


import org.codewithzea.trackerboost.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6) String password,
        @NotNull Role role
) {}
