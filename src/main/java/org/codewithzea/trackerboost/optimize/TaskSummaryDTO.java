package org.codewithzea.trackerboost.optimize;


import java.time.LocalDate;

public record TaskSummaryDTO(
        Long id,
        String title,
        String status,
        LocalDate dueDate
) {}
