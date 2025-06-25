package org.codewithzea.trackerboost.project;




import org.codewithzea.trackerboost.task.Task;
import org.codewithzea.trackerboost.user.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    private LocalDate deadline;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProjectStatus status; // e.g., "ACTIVE", "COMPLETED", "ON_HOLD"

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    @NotNull
    private UserEntity manager;
}


