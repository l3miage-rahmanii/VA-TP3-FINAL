package fr.uga.l3miage.spring.tp3.components;

import fr.uga.l3miage.spring.tp3.enums.SessionStatus;
import fr.uga.l3miage.spring.tp3.exceptions.technical.SessionNotFoundExeption;
import fr.uga.l3miage.spring.tp3.models.EcosSessionEntity;
import fr.uga.l3miage.spring.tp3.models.EcosSessionProgrammationStepEntity;
import fr.uga.l3miage.spring.tp3.repositories.EcosSessionProgrammationRepository;
import fr.uga.l3miage.spring.tp3.repositories.EcosSessionProgrammationStepRepository;
import fr.uga.l3miage.spring.tp3.repositories.EcosSessionRepository;
import fr.uga.l3miage.spring.tp3.responses.CandidateEvaluationGridDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SessionComponent {
    private final EcosSessionRepository ecosSessionRepository;
    private final EcosSessionProgrammationRepository ecosSessionProgrammationRepository;
    private final EcosSessionProgrammationStepRepository ecosSessionProgrammationStepRepository;


    public EcosSessionEntity createSession(EcosSessionEntity entity){
        ecosSessionProgrammationStepRepository.saveAll(entity.getEcosSessionProgrammationEntity().getEcosSessionProgrammationStepEntities());
        ecosSessionProgrammationRepository.save(entity.getEcosSessionProgrammationEntity());
        return ecosSessionRepository.save(entity);
    }

    public EcosSessionEntity findSessionEntityById(Long id) throws SessionNotFoundExeption{
        return ecosSessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundExeption((String.format("La session %s est introuvable", id))));
    }

    private boolean isLastStepPassed(EcosSessionEntity session) {
        if (session.getEcosSessionProgrammationEntity() == null ||
        session.getEcosSessionProgrammationEntity().getEcosSessionProgrammationStepEntities() == null ||
        session.getEcosSessionProgrammationEntity().getEcosSessionProgrammationStepEntities().isEmpty()) {
            return false;
        }


        //passer une étape signifie atteindre ou dépasser cette dateTime par rapport à la date/heure actuelle.
        LocalDateTime now = LocalDateTime.now();

        // Trouvez la dernière étape basée sur la propriété dateTime
        Optional<EcosSessionProgrammationStepEntity> lastStepOptional = session.getEcosSessionProgrammationEntity()
                .getEcosSessionProgrammationStepEntities()
                .stream()
                .max(Comparator.comparing(EcosSessionProgrammationStepEntity::getDateTime));

        if (lastStepOptional.isPresent()) {
            EcosSessionProgrammationStepEntity lastStep = lastStepOptional.get();
            return !lastStep.getDateTime().isAfter(now);
        } else {

            return false;
        }
    }

    public EcosSessionEntity endSessionEvaluation(Long id) throws SessionNotFoundExeption {
        EcosSessionEntity session = ecosSessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundExeption(String.format("La session %d est introuvable", id)));

        // Vérification que la dernière étape est passée
        if (!isLastStepPassed(session)) {
            throw new IllegalStateException("The last step is not passed yet.");
        }

        // Vérification que l'état précédent est EVAL_STARTED
        if (!session.getStatus().equals(SessionStatus.EVAL_STARTED)) {
            throw new IllegalStateException("Session is not in EVAL_STARTED state.");
        }

        // Mise à jour de l'état de la session
        session.setStatus(SessionStatus.EVAL_ENDED);
        // Enregistrer les modifications dans la base de données
        ecosSessionRepository.save(session);

        // Retourner l'entité EcosSession mise à jour
        return session;
    }

}
