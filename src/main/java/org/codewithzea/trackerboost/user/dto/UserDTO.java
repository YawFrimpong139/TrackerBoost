package org.codewithzea.trackerboost.user.dto;

import lombok.Builder;
import lombok.Data;
import org.codewithzea.trackerboost.user.Role;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
}
