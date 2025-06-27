package org.codewithzea.trackerboost.developer;



import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.*;
import org.codewithzea.trackerboost.task.*;
import org.codewithzea.trackerboost.user.UserEntity;

@Entity
@Table(name = "developers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Developer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;

    @NotBlank @Size(max = 100)
    private String name;

    @Email @NotBlank
    @Column(unique = true)
    private String email;

    @ElementCollection
    @CollectionTable(name = "developer_skills", joinColumns = @JoinColumn(name = "developer_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    @ManyToMany(mappedBy = "assignedDevelopers")
    private Set<Task> assignedTasks = new HashSet<>();
}


