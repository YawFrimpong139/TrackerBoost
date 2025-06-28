package org.codewithzea.trackerboost.optimize;


import org.codewithzea.trackerboost.project.ProjectDTO;
import org.codewithzea.trackerboost.project.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/benchmark")
@RequiredArgsConstructor
public class BenchmarkController {

    private final ProjectService projectService;
    private final ObjectMapper objectMapper;

    @GetMapping("/projects")
    public BenchmarkResult compareProjectEndpoints(Pageable pageable) throws IOException {
        // Test original endpoint (returns full entities)
        long startTime = System.nanoTime();
        Page<ProjectDTO> originalData = projectService.getAllProjects(pageable);
        long originalTime = System.nanoTime() - startTime;
        long originalSize = getObjectSize(originalData);

        // Test optimized endpoint (returns DTOs)
        startTime = System.nanoTime();
        Page<ProjectListDTO> optimizedData = projectService.getAllProjectSummaries(Pageable.unpaged());
        long optimizedTime = System.nanoTime() - startTime;
        long optimizedSize = getObjectSize(optimizedData.getContent());

        return new BenchmarkResult(
                "Projects Comparison",
                originalTime,
                optimizedTime,
                originalSize,
                optimizedSize
        );
    }

    private long getObjectSize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        objectMapper.writeValue(baos, obj);
        return baos.size();
    }
}
