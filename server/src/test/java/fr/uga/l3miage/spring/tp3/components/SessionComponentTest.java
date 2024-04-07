package fr.uga.l3miage.spring.tp3.components;

import fr.uga.l3miage.spring.tp3.components.SessionComponent;
import fr.uga.l3miage.spring.tp3.exceptions.technical.SessionNotFoundExeption;
import fr.uga.l3miage.spring.tp3.models.EcosSessionEntity;
import fr.uga.l3miage.spring.tp3.models.EcosSessionProgrammationEntity;
import fr.uga.l3miage.spring.tp3.models.EcosSessionProgrammationStepEntity;
import fr.uga.l3miage.spring.tp3.repositories.EcosSessionProgrammationRepository;
import fr.uga.l3miage.spring.tp3.repositories.EcosSessionProgrammationStepRepository;
import fr.uga.l3miage.spring.tp3.repositories.EcosSessionRepository;
import fr.uga.l3miage.spring.tp3.enums.SessionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class SessionComponentTest {

    @Mock
    private EcosSessionRepository ecosSessionRepository;

    @Mock
    private EcosSessionProgrammationRepository ecosSessionProgrammationRepository;

    @Mock
    private EcosSessionProgrammationStepRepository ecosSessionProgrammationStepRepository;

    @InjectMocks
    private SessionComponent sessionComponent;

    @Test
    void createSessionTest() {
        // Given
        EcosSessionProgrammationStepEntity stepEntity = new EcosSessionProgrammationStepEntity();

        EcosSessionProgrammationEntity programmationEntity = new EcosSessionProgrammationEntity();
        programmationEntity.setEcosSessionProgrammationStepEntities(Set.of(stepEntity));

        EcosSessionEntity session = new EcosSessionEntity();
        session.setEcosSessionProgrammationEntity(programmationEntity);

        when(ecosSessionRepository.save(any(EcosSessionEntity.class))).thenReturn(session);

        // When
        EcosSessionEntity savedSession = sessionComponent.createSession(session);

        // Then
        assertThat(savedSession.getEcosSessionProgrammationEntity().getEcosSessionProgrammationStepEntities())
                .isNotNull()
                .containsExactlyInAnyOrder(stepEntity);
    }

    @Test
    void testEndSessionEvaluationSuccess() throws SessionNotFoundExeption {
        // Given
        Long sessionId = 123456L;

        EcosSessionProgrammationStepEntity step1 = EcosSessionProgrammationStepEntity
                .builder()
                        .dateTime(LocalDateTime.now().minus(Duration.ofDays(1)))
                                .build();
        EcosSessionProgrammationStepEntity step2 = EcosSessionProgrammationStepEntity
                .builder()
                .dateTime(LocalDateTime.now().minus(Duration.ofDays(1)))
                .build();

        Set<EcosSessionProgrammationStepEntity> steps = new HashSet<>();
        steps.add(step1);
        steps.add(step2);


        EcosSessionProgrammationEntity ecosSessionProgrammationEntity = EcosSessionProgrammationEntity
                .builder()
                .ecosSessionProgrammationStepEntities(steps)
                .build();

        EcosSessionEntity ecosSessionEntity = EcosSessionEntity.builder()
                .id(sessionId)
                .ecosSessionProgrammationEntity(ecosSessionProgrammationEntity)
                .status(SessionStatus.EVAL_STARTED)
                .build();


        ecosSessionRepository.save(ecosSessionEntity);

        when(ecosSessionRepository.findById(sessionId)).thenReturn(Optional.of(ecosSessionEntity));

        // When
        EcosSessionEntity updatedSession = sessionComponent.endSessionEvaluation(sessionId);

        // Then
        assertEquals(SessionStatus.EVAL_ENDED, updatedSession.getStatus());
    }

    @Test
    void testEndSessionEvaluationFailed() {
        // Given
        Long sessionId = 123456L;


        EcosSessionEntity ecosSessionEntity = EcosSessionEntity.builder()
                .id(sessionId)
                .status(SessionStatus.CREATED)
                .build();

        ecosSessionRepository.save(ecosSessionEntity);

        when(ecosSessionRepository.findById(sessionId)).thenReturn(Optional.of(ecosSessionEntity));

        // When/Then
        assertThrows(IllegalStateException.class, () -> {
            sessionComponent.endSessionEvaluation(sessionId);
        });
    }

}
