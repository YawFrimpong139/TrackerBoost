package org.codewithzea.trackerboost.developer;



import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.Set;

@Builder
public record DeveloperDTO(
        Long id,

        @NotBlank @Size(max = 100)
        String name,

        @Email @NotBlank
        String email,

        Set<String> skills,

        Long userId
) {

}

