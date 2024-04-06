package fr.uga.l3miage.spring.tp3.services;


import fr.uga.l3miage.spring.tp3.components.ExamComponent;
import fr.uga.l3miage.spring.tp3.components.SessionComponent;
import fr.uga.l3miage.spring.tp3.enums.SessionStatus;
import fr.uga.l3miage.spring.tp3.exceptions.rest.CreationSessionRestException;
import fr.uga.l3miage.spring.tp3.exceptions.technical.ExamNotFoundException;
import fr.uga.l3miage.spring.tp3.exceptions.technical.SessionNotFoundExeption;
import fr.uga.l3miage.spring.tp3.mappers.SessionMapper;
import fr.uga.l3miage.spring.tp3.models.EcosSessionEntity;
import fr.uga.l3miage.spring.tp3.models.ExamEntity;
import fr.uga.l3miage.spring.tp3.request.SessionCreationRequest;
import fr.uga.l3miage.spring.tp3.request.SessionProgrammationCreationRequest;
import fr.uga.l3miage.spring.tp3.responses.CandidateEvaluationGridDTO;
import fr.uga.l3miage.spring.tp3.responses.ExamResponse;
import fr.uga.l3miage.spring.tp3.responses.SessionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SessionServiceTest {
    @Autowired
    private SessionService sessionService;

    @MockBean
    private SessionComponent sessionComponent;

    @SpyBean
    private SessionMapper sessionMapper;

    @MockBean
    private ExamComponent examComponent;


    @Test
    void testCreateSession() throws ExamNotFoundException {
        //given
        SessionProgrammationCreationRequest programmation = SessionProgrammationCreationRequest.builder()
                .steps(Set.of())
                .build();

        SessionCreationRequest request = SessionCreationRequest
                .builder()
                .name("session normal")
                .examsId(Set.of())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .ecosSessionProgrammation(programmation)
                .build();

        EcosSessionEntity ecosSessionEntity = sessionMapper.toEntity(request);
        ecosSessionEntity.setExamEntities(Set.of());

        when(examComponent.getAllById(same(Set.of()))).thenReturn(Set.of());
        when(sessionComponent.createSession(any(EcosSessionEntity.class))).thenReturn(ecosSessionEntity);

        SessionResponse expectedResponse = sessionMapper.toResponse(ecosSessionEntity);
        // when
        SessionResponse actualResponse = sessionService.createSession(request);

        // then
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);

    }

    @Test
    void testCreateSessionFailed() throws ExamNotFoundException{

        SessionProgrammationCreationRequest programmation = SessionProgrammationCreationRequest.builder()
                .steps(Set.of())
                .build();

        SessionCreationRequest request = SessionCreationRequest
                .builder()
                .name("session normal")
                .examsId(Set.of())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .ecosSessionProgrammation(programmation)
                .build();

        when(examComponent.getAllById(Set.of())).thenThrow(new ExamNotFoundException("Exams not found"));


        // Then
        assertThrows(CreationSessionRestException.class, () -> sessionService.createSession(request));

    }

    @Test
    void testEndSessionEvaluationSucces() throws SessionNotFoundExeption {
        //given
        Long id = 123174L;

        EcosSessionEntity sessionEntity = EcosSessionEntity
                .builder()
                .id(id)
                .name("session normale")
                .status(SessionStatus.EVAL_STARTED)
                .build();

        ExamEntity examEntity = ExamEntity
                .builder()
                .name("math")
                .weight(3)
                .id(id)
                .build();

        CandidateEvaluationGridDTO evaluation1 = CandidateEvaluationGridDTO.builder().grade(16.0).build();
        CandidateEvaluationGridDTO evaluation2 = CandidateEvaluationGridDTO.builder().grade(17.0).build();

        Set<CandidateEvaluationGridDTO> evaluations = new HashSet<>(Arrays.asList(evaluation1, evaluation2));
        Set<ExamEntity> exams = new HashSet<>(Collections.singletonList(examEntity));
        sessionEntity.setExamEntities(exams);

        // Simuler le comportement de sessionComponent pour retourner la session mise à jour
        when(sessionComponent.endSessionEvaluation(id)).thenReturn(sessionEntity);

        // Simuler le comportement de sessionMapper pour convertir la session et l'examen en leurs DTOs correspondants
        SessionResponse sessionResponse = SessionResponse.builder()
                .id(id)
                .name("Session normale")
                .status(fr.uga.l3miage.spring.tp3.responses.enums.SessionStatus.EVAL_ENDED)
                .examEntities(exams.stream().map(exam -> {
                    ExamResponse examResponse = ExamResponse.builder()
                            .name(exam.getName())
                            .weight(exam.getWeight())
                            .evaluations(evaluations) // Associe les évaluations à l'examen
                            .build();
                    return examResponse;
                }).collect(Collectors.toSet()))
                .build();

        when(sessionMapper.toResponse(any(EcosSessionEntity.class))).thenReturn(sessionResponse);


        Set<CandidateEvaluationGridDTO> actualEvaluations = sessionService.endSessionEvaluation(id);

        // Assertions pour vérifier que les évaluations attendues sont retournées
        assertNotNull(actualEvaluations, "Le résultat des évaluations ne devrait pas être null");
        assertEquals(evaluations.size(), actualEvaluations.size(), "Le nombre d'évaluations retournées ne correspond pas");
        assertTrue(actualEvaluations.containsAll(evaluations), "Les évaluations retournées ne correspondent pas aux évaluations attendues");

        // Vérifier que les méthodes mockées ont été appelées comme prévu
        verify(sessionComponent).endSessionEvaluation(id);
        verify(sessionMapper).toResponse(any(EcosSessionEntity.class));

    }
}
