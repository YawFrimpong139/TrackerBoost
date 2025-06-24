package org.codewithzea.trackerboost.user;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(@NotBlank String email);

    Optional<UserEntity> findByEmail(@NotBlank String email);
}
