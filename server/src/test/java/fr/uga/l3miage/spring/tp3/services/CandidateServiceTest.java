package fr.uga.l3miage.spring.tp3.services;


import fr.uga.l3miage.spring.tp3.components.CandidateComponent;
import fr.uga.l3miage.spring.tp3.exceptions.rest.CandidateNotFoundRestException;
import fr.uga.l3miage.spring.tp3.exceptions.technical.CandidateNotFoundException;
import fr.uga.l3miage.spring.tp3.models.CandidateEntity;
import fr.uga.l3miage.spring.tp3.models.CandidateEvaluationGridEntity;
import fr.uga.l3miage.spring.tp3.models.ExamEntity;
import fr.uga.l3miage.spring.tp3.repositories.CandidateEvaluationGridRepository;
import fr.uga.l3miage.spring.tp3.repositories.CandidateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CandidateServiceTest {
    @Autowired
    private CandidateService candidateService;

    @MockBean
    private CandidateComponent candidateComponent;

    @MockBean
    private CandidateRepository candidateRepository;

    @MockBean
    private CandidateEvaluationGridRepository candidateEvaluationGridRepository;
    @Test
    void TestGetCandidateAverageSuccess() throws CandidateNotFoundException {
        // given
        Long candidateId = 1L;
        ExamEntity mathExam = ExamEntity.builder().name("Math").weight(2).build();
        ExamEntity science = ExamEntity.builder().name("science").weight(3).build();
        CandidateEvaluationGridEntity grid1 = CandidateEvaluationGridEntity.builder()
                .grade(10)
                .examEntity(mathExam)
                .build();
        CandidateEvaluationGridEntity grid2 = CandidateEvaluationGridEntity.builder()
                .grade(8)
                .examEntity(science)
                .build();
        Set<CandidateEvaluationGridEntity> grids = new HashSet<>();
        grids.add(grid1);
        grids.add(grid2);

        CandidateEntity mockCandidate = CandidateEntity.builder()
                .candidateEvaluationGridEntities(grids)
                .build();
        when(candidateComponent.getCandidatById(candidateId)).thenReturn(mockCandidate);

        // La logique de calcul attendue
        double expectedAverage = ((10 * 2.0) + (8 * 3.0)) / (2+3);

        // when
        Double actualAverage = candidateService.getCandidateAverage(candidateId);

        // then
        assertEquals(expectedAverage, actualAverage, 0.01, "The calculated average should match the expected value.");
    }

    @Test
    void getCandidateAverageCandidateNotFound() throws  CandidateNotFoundException {
        // given
        Long candidateId = 2L;
        when(candidateComponent.getCandidatById(candidateId)).thenThrow(new CandidateNotFoundException("Candidate not found", candidateId));

        // when-then
        assertThrows(CandidateNotFoundRestException.class, () -> candidateService.getCandidateAverage(candidateId), "Should throw CandidateNotFoundRestException when candidate is not found.");
    }
}