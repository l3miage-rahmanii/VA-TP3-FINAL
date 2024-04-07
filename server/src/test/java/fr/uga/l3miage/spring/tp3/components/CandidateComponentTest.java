package fr.uga.l3miage.spring.tp3.components;
import fr.uga.l3miage.spring.tp3.components.CandidateComponent;
import fr.uga.l3miage.spring.tp3.exceptions.technical.CandidateNotFoundException;
import fr.uga.l3miage.spring.tp3.models.CandidateEntity;
import fr.uga.l3miage.spring.tp3.models.CandidateEvaluationGridEntity;
import fr.uga.l3miage.spring.tp3.repositories.CandidateEvaluationGridRepository;
import fr.uga.l3miage.spring.tp3.repositories.CandidateRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CandidateComponentTest {

    @Autowired
    @MockBean
    private CandidateComponent candidateComponent;

    @MockBean
    private CandidateEvaluationGridRepository candidateEvaluationGridRepository;

    @MockBean
    private CandidateRepository candidateRepository;

    @Test
    void getCandidatByIdFound() throws Exception {
        // Given
        Long candidatId = 1L;
        CandidateEntity expectedCandidate = CandidateEntity.builder().id(candidatId).build();
        when(candidateRepository.findById(candidatId)).thenReturn(Optional.of(expectedCandidate));

        // When
        CandidateEntity result = candidateComponent.getCandidatById(candidatId);

        // Then
        assertEquals(expectedCandidate, result);
    }

    @Test
    void getCandidatByIdNotFound(){
        // Given
        Long candidatId = 1234L;

        CandidateEntity candidate1 = CandidateEntity
                .builder()
                .email("test@gmail.com")
                .firstname("Idris")
                .build();

        CandidateEntity candidate2 = CandidateEntity
                .builder()
                .email("toto@gmail.com")
                .firstname("Bachir")
                .build();

        candidateRepository.save(candidate1);
        candidateRepository.save(candidate2);

        //when
        when(candidateRepository.findById(candidatId)).thenReturn(Optional.empty());

        // Then

        assertThrows(CandidateNotFoundException.class, () -> {
            candidateComponent.getCandidatById(candidatId);
        });
    }


    @Test
    void getAllEliminatedCandidateFound(){
        // Given
        when(candidateEvaluationGridRepository.findAllByGradeIsLessThanEqual(5)).thenReturn(Set.of());
        // When
        Set<CandidateEntity> candidateEntities = candidateComponent.getAllEliminatedCandidate();
        // Then
        assertThat(candidateEntities).isEmpty();
    }

    

}