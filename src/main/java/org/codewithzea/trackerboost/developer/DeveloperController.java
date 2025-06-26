package org.codewithzea.trackerboost.developer;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/developers")
@RequiredArgsConstructor
public class DeveloperController {

    private final DeveloperService developerService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<DeveloperDTO> getDeveloperById(@PathVariable Long id) {
        log.info("Fetching developer with ID: {}", id);
        DeveloperDTO developer = developerService.getDeveloperById(id);
        log.debug("Retrieved developer: {}", developer);
        return ResponseEntity.ok(developer);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<Page<DeveloperDTO>> getAllDevelopers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Fetching all developers - Page: {}, Size: {}, Sort: {}, Direction: {}",
                page, size, sortBy, direction);
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DeveloperDTO> developers = developerService.getAllDevelopers(pageable);
        log.debug("Retrieved {} developers", developers.getNumberOfElements());
        return ResponseEntity.ok(developers);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DeveloperDTO> createDeveloper(@Valid @RequestBody DeveloperDTO dto) throws Exception {
        log.info("Creating new developer: {}", dto);
        DeveloperDTO createdDeveloper = developerService.createDeveloper(dto);
        log.info("Created developer with ID: {}", createdDeveloper.getId());
        return ResponseEntity.ok(createdDeveloper);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DeveloperDTO> updateDeveloper(@PathVariable Long id, @Valid @RequestBody DeveloperDTO dto) throws Exception {
        log.info("Updating developer with ID: {}, Data: {}", id, dto);
        DeveloperDTO updatedDeveloper = developerService.updateDeveloper(id, dto);
        log.info("Updated developer with ID: {}", id);
        return ResponseEntity.ok(updatedDeveloper);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable Long id) {
        log.info("Deleting developer with ID: {}", id);
        developerService.deleteDeveloper(id);
        log.info("Deleted developer with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/top")
    public ResponseEntity<List<DeveloperDTO>> getTop5DevelopersByTaskCount() {
        log.info("Fetching top 5 developers by task count");
        List<DeveloperDTO> developers = developerService.getTop5DevelopersByTaskCount();
        log.debug("Retrieved top developers: {}", developers);
        return ResponseEntity.ok(developers);
    }
}
