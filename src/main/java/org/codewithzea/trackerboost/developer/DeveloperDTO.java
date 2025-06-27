package org.codewithzea.trackerboost.developer;



import lombok.*;
import jakarta.validation.constraints.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperDTO {
    private Long id;

    @NotBlank @Size(max = 100)
    private String name;

    @Email @NotBlank
    private String email;

    private Set<String> skills;

    private Long userId;
}



