package org.codewithzea.trackerboost.task;






import org.codewithzea.trackerboost.developer.Developer;
import org.codewithzea.trackerboost.project.Project;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 150)
    private String title;

    @Size(max = 500)
    private String description;

    @NotBlank
    private String status;  // e.g., "PENDING", "IN_PROGRESS", "DONE"

    @NotNull
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Optional assignment of developer
    @ManyToMany
    @JoinTable(
            name = "task_developer",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "developer_id")
    )
    private Set<Developer> assignedDevelopers = new HashSet<>();
}



