package org.codewithzea.trackerboost.task;





import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDTO {
    private Long id;

    @NotBlank @Size(max = 150)
    private String title;

    @Size(max = 500)
    private String description;

    @NotBlank
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate dueDate;

    @NotNull
    private Long projectId;

    private Set<Long> assignedDeveloperIds;


    public TaskDTO(Long id, String title, String description, String status,
                   LocalDate dueDate, Long projectId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.projectId = projectId;
    }
}




