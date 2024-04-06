package fr.uga.l3miage.spring.tp3.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class ExamResponse {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String name;
    private int weight;
    private Set<CandidateEvaluationGridDTO> evaluations;
}
