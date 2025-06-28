package org.codewithzea.trackerboost.developer;


import org.codewithzea.trackerboost.audit.AuditLogService;
import org.codewithzea.trackerboost.exception.ResourceNotFoundException;
import org.codewithzea.trackerboost.user.UserEntity;
import org.codewithzea.trackerboost.user.UserRepository;
import org.codewithzea.trackerboost.util.MapperUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final UserRepository  userRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;


    public DeveloperDTO getDeveloperById(Long id) {
        Developer dev = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id " + id));
        return MapperUtil.toDeveloperDTO(dev);
    }

    public Page<DeveloperDTO> getAllDevelopers(Pageable pageable) {
        return developerRepository.findAll(pageable).map(MapperUtil::toDeveloperDTO);
    }

    @Transactional
    public DeveloperDTO createDeveloper(DeveloperDTO dto) throws Exception {
        if (developerRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already in use.");
        }
        UserEntity user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Developer dev = MapperUtil.toDeveloper(dto, user);
        Developer saved = developerRepository.save(dev);
        String payload = objectMapper.writeValueAsString(saved);
        auditLogService.log("CREATE", "Developer", saved.getId().toString(), payload);
        return MapperUtil.toDeveloperDTO(saved);
    }

    @Transactional
    public DeveloperDTO updateDeveloper(Long id, DeveloperDTO dto) throws Exception {
        Developer dev = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id " + id));

        dev.setName(dto.name());
        dev.setEmail(dto.email());
        dev.setSkills(dto.skills());

        Developer updated = developerRepository.save(dev);
        String payload = objectMapper.writeValueAsString(updated);
        auditLogService.log("UPDATE", "Developer", updated.getId().toString(), payload);
        return MapperUtil.toDeveloperDTO(updated);
    }

    @Transactional
    public void deleteDeveloper(Long id) {
        Developer dev = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id " + id));
        developerRepository.delete(dev);
        auditLogService.log("DELETE", "Developer", id.toString(), "");
    }

    public List<DeveloperDTO> getTop5DevelopersByTaskCount() {
        // Use exact size since we know it's 5
        return developerRepository.findTop5ByTaskCount(PageRequest.of(0,5)).stream()
                .map(MapperUtil::toDeveloperDTO)
                .collect(Collectors.toCollection(() -> new ArrayList<>(5)));
    }

}
