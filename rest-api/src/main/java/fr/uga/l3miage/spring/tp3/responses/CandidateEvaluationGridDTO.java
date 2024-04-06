package fr.uga.l3miage.spring.tp3.responses;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
public class CandidateEvaluationGridDTO {

    private Long sheetNumber;

    private Double grade;
}
