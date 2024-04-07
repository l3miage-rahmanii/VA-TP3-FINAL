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
import java.util.*;

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
        //given
        SessionCreationRequest request = SessionCreationRequest.builder()
                .name("test")
                .startDate(LocalDateTime.MIN)
                .endDate(LocalDateTime.MAX)
                .examsId(new HashSet<>())
                .ecosSessionProgrammation(SessionProgrammationCreationRequest.builder().steps(new HashSet<>()).build())
                .build();

        //when
        ResponseEntity<SessionResponse> responseEntity = testRestTemplate.postForEntity("/api/sessions/create", request, SessionResponse.class);

        //when
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(201);
        assertThat(responseEntity.getBody()).isNotNull();
    }

    @Test
    void createSessionFailed() {
        //given
        when(ecosSessionService.createSession(any(SessionCreationRequest.class)))
                .thenThrow(new CreationSessionRestException("Session creation failed"));

        SessionCreationRequest request = SessionCreationRequest.builder(). name("test")
                .startDate(LocalDateTime.MIN)
                .endDate(LocalDateTime.MAX)
                .examsId(new HashSet<>())
                .ecosSessionProgrammation(SessionProgrammationCreationRequest.builder().steps(new HashSet<>()).build())
                .build();


        //when
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/api/sessions/create", request, String.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void endSessionEvaluationSuccess() {
        //given
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


        //when
        ResponseEntity<String> response = testRestTemplate.exchange( //Envoi de la requête pour terminer l'évaluation
                "/api/sessions/{id}/end-eval",
                HttpMethod.PUT,
                null,
                String.class,
                session.getId());

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void whenUpdateSessionStateCausesConflict() {

        //given
        EcosSessionEntity session = EcosSessionEntity.builder()
                .name("session normale")
                .status(SessionStatus.EVAL_ENDED)
                .build();

        ecosSessionRepository.save(session);

        EcosSessionProgrammationEntity programmationEntity = EcosSessionProgrammationEntity.builder()
                .label("Test")
                .build();

        sessionProgrammationRepository.save(programmationEntity);

        // Définir une date/heure dans le passé pour lastStep
        LocalDateTime stepDateTime = LocalDateTime.now().minusDays(1);


        EcosSessionProgrammationStepEntity lastStep = EcosSessionProgrammationStepEntity.builder()
                .dateTime(stepDateTime)
                .ecosSessionProgrammationEntity(programmationEntity)
                .description("Final step")
                .build();


        sessionProgrammationStepRepository.save(lastStep);

        Set<EcosSessionProgrammationStepEntity> steps = Set.of(lastStep);

        programmationEntity.setEcosSessionProgrammationStepEntities(steps);

        sessionProgrammationRepository.save(programmationEntity);

        session.setEcosSessionProgrammationEntity(programmationEntity);

        ecosSessionRepository.save(session);

        final Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("id",session.getId() );

        //when
        // Envoi de la requête pour terminer l'évaluation
        ResponseEntity<String> response = testRestTemplate.exchange(
                "/api/sessions/{id}/end-eval",
                HttpMethod.PUT,
                null,
                String.class,
                urlParams); // Utiliser l'ID de la session sauvegardée

        //then
        // Vérifier que le code de statut est 409 CONFLIT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Vérifier les détails de la réponse
        assertThat(response.getBody()).contains("URI");
        assertThat(response.getBody()).contains("Message d'erreur");
        assertThat(response.getBody()).contains("État actuel de la session");
    }



}