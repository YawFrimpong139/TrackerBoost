package org.codewithzea.trackerboost.project;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Objects;

@Builder
public record ProjectDTO(
        Long id,

        @NotBlank @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate deadline,

        @NotBlank
        String status,

        @NotNull(message = "Manager ID is required")
        Long managerId
) {
    public ProjectDTO {
        Objects.requireNonNull(deadline, "Deadline cannot be null");
        Objects.requireNonNull(managerId, "Manager ID cannot be null");
    }
}