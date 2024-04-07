Participants :

-Rahmani Imen
-Ouerghi Anouer

Problémes rencontrées :

1) Les tests sur les contrôleurs et les composants fonctionnaient sur la machine de Ouerghi, mais pas sur celle de Rahmani (pas tous, mais certains).
2) Le test 'void testCreateSessionSucces() throws ExamNotFoundException' fonctionnait lors du TP de vendredi, 
mais quand nous l'avons exécuté aujourd'hui, nous avons rencontré cette erreur : java.lang.NoSuchMethodError: fr.uga.l3miage.spring.tp3.responses.SessionResponse: method 'void <init>()' not found
                                                                                 at fr.uga.l3miage.spring.tp3.mappers.SessionMapperImpl.toResponse(SessionMapperImpl.java:83)ù

    