package fr.uga.l3miage.spring.tp3.controller;

import fr.uga.l3miage.spring.tp3.endpoints.SessionEndpoints;
import fr.uga.l3miage.spring.tp3.exceptions.technical.SessionNotFoundExeption;
import fr.uga.l3miage.spring.tp3.request.SessionCreationRequest;
import fr.uga.l3miage.spring.tp3.responses.SessionResponse;
import fr.uga.l3miage.spring.tp3.services.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SessionController implements SessionEndpoints {
    private final SessionService sessionService;

    @Override
    public SessionResponse createSession(SessionCreationRequest request) {
        return sessionService.createSession(request);
    }

    @Override
    public ResponseEntity<?> endSessionEvaluation(Long sessionId) {
        try {
            sessionService.endSessionEvaluation(sessionId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
