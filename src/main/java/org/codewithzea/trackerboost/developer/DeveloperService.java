package org.codewithzea.trackerboost.developer;


import org.codewithzea.trackerboost.audit.AuditLogService;
import org.codewithzea.trackerboost.exception.ResourceNotFoundException;
import org.codewithzea.trackerboost.util.MapperUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeveloperService {

    private final DeveloperRepository developerRepository;
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
        if (developerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }
        Developer dev = MapperUtil.toDeveloper(dto);
        Developer saved = developerRepository.save(dev);
        String payload = objectMapper.writeValueAsString(saved);
        auditLogService.log("CREATE", "Developer", saved.getId().toString(), payload);
        return MapperUtil.toDeveloperDTO(saved);
    }

    @Transactional
    public DeveloperDTO updateDeveloper(Long id, DeveloperDTO dto) throws Exception {
        Developer dev = developerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id " + id));

        dev.setName(dto.getName());
        dev.setEmail(dto.getEmail());
        dev.setSkills(dto.getSkills());

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
        List<Developer> topDevs = developerRepository.findTop5ByTaskCount(PageRequest.of(0,5));
        return topDevs.stream().map(MapperUtil::toDeveloperDTO).collect(Collectors.toList());
    }
}


