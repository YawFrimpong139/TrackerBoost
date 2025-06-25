package org.codewithzea.trackerboost.project;




import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {
    private Long id;

    @NotBlank @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    @NotBlank
    private String status;

    @NotNull(message = "Manager ID is required")
    private Long managerId;
}



