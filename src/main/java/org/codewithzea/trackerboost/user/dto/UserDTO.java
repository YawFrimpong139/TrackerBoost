package org.codewithzea.trackerboost.user.dto;

import org.codewithzea.trackerboost.user.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
public record UserDTO(
        Long id,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @NotNull Role role
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}