package fr.uga.l3miage.spring.tp3.controllers;


import fr.uga.l3miage.spring.tp3.enums.SessionStatus;
import fr.uga.l3miage.spring.tp3.exceptions.rest.CreationSessionRestException;
import fr.uga.l3miage.spring.tp3.exceptions.technical.SessionNotFoundExeption;
import fr.uga.l3miage.spring.tp3.models.EcosSessionEntity;
import fr.uga.l3miage.spring.tp3.models.EcosSessionProgrammationEntity;
import fr.uga.l3miage.spring.tp3.models.EcosSessionProgrammationStepEntity;
import fr.uga.l3miage.spring.tp3.repositories.EcosSessionProgrammationRepository;
import fr.uga.l3miage.spring.tp3.repositories.EcosSessionProgrammationStepRepository;
import fr.uga.l3miage.spring.tp3.repositories.EcosSessionRepository;
import fr.uga.l3miage.spring.tp3.request.SessionCreationRequest;
import fr.uga.l3miage.spring.tp3.request.SessionProgrammationCreationRequest;
import fr.uga.l3miage.spring.tp3.responses.SessionResponse;
import fr.uga.l3miage.spring.tp3.services.SessionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@AutoConfigureTestDatabase
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
public class SessionControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @MockBean
    private SessionService ecosSessionService;
    @Autowired
    private EcosSessionRepository ecosSessionRepository;

    @Autowired
    EcosSessionProgrammationRepository sessionProgrammationRepository;

    @Autowired
    EcosSessionProgrammationStepRepository sessionProgrammationStepRepository;

    @AfterEach
    public void clear(){
        ecosSessionRepository.deleteAll();
    }
    @Test
    void createSessionSuccess(){
        SessionCreationRequest request = SessionCreationRequest.builder()
                .name("test")
                .startDate(LocalDateTime.MIN)
                .endDate(LocalDateTime.MAX)
                .examsId(new HashSet<>())
                .ecosSessionProgrammation(SessionProgrammationCreationRequest.builder().steps(new HashSet<>()).build())
                .build();

        ResponseEntity<SessionResponse> responseEntity = testRestTemplate.postForEntity("/api/sessions/create", request, SessionResponse.class);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(201);
        assertThat(responseEntity.getBody()).isNotNull();
    }

    @Test
    void createSessionFailed() {
        when(ecosSessionService.createSession(any(SessionCreationRequest.class)))
                .thenThrow(new CreationSessionRestException("Session creation failed"));

        SessionCreationRequest request = SessionCreationRequest.builder(). name("test")
                .startDate(LocalDateTime.MIN)
                .endDate(LocalDateTime.MAX)
                .examsId(new HashSet<>())
                .ecosSessionProgrammation(SessionProgrammationCreationRequest.builder().steps(new HashSet<>()).build())
                .build();




        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/api/sessions/create", request, String.class);

        // Vérifier que la réponse est bien un statut 400
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void endSessionEvaluationSuccess() {
        EcosSessionEntity session = EcosSessionEntity.builder()
                .name("Session normale")
                .status(SessionStatus.EVAL_STARTED)
                .build();

        ecosSessionRepository.save(session);

        EcosSessionProgrammationEntity ecosSessionProgrammationEntity = EcosSessionProgrammationEntity
                .builder()
                .label("test")
                .build();

        sessionProgrammationRepository.save(ecosSessionProgrammationEntity);


        // On définit la date et l'heur dans le passé
        LocalDateTime stepDateTime = LocalDateTime.now().minusDays(1);


        EcosSessionProgrammationStepEntity lastStep = EcosSessionProgrammationStepEntity
                .builder()
                .dateTime(stepDateTime)
                .ecosSessionProgrammationEntity(ecosSessionProgrammationEntity)
                .description("fin")
                .build();

        sessionProgrammationStepRepository.save(lastStep);

        Set<EcosSessionProgrammationStepEntity> steps = Set.of(lastStep);

        ecosSessionProgrammationEntity.setEcosSessionProgrammationStepEntities(steps);

        sessionProgrammationRepository.save(ecosSessionProgrammationEntity);

        session.setEcosSessionProgrammationEntity(ecosSessionProgrammationEntity);

        ecosSessionRepository.save(session);


        // Envoi de la requête pour terminer l'évaluation
        ResponseEntity<String> response = testRestTemplate.exchange(
                "/api/sessions/{id}/end-eval",
                HttpMethod.PUT,
                null,
                String.class,
                session.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void endSessionEvaluationFailure() {
        // Given
        Long sessionId = 999999L; // Un ID de session inexistant

        // Quand la fin de l'évaluation de session est tentée, nous simulons une session non trouvée en retournant null
        when(ecosSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // Message d'erreur pour quand la session n'est pas trouvée
        String expectedErrorMessage = "Session not found";

        // When
        ResponseEntity<String> response = testRestTemplate.exchange(
                "/api/sessions/{id}/end-eval",
                HttpMethod.PUT,
                null,
                String.class,
                sessionId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertThat(response.getBody().contains(expectedErrorMessage));
    }



}