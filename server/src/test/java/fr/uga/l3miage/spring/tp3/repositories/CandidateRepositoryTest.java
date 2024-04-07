package fr.uga.l3miage.spring.tp3.repositories;

import fr.uga.l3miage.spring.tp3.enums.TestCenterCode;
import fr.uga.l3miage.spring.tp3.models.CandidateEntity;
import fr.uga.l3miage.spring.tp3.models.CandidateEvaluationGridEntity;
import fr.uga.l3miage.spring.tp3.models.TestCenterEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
public class CandidateRepositoryTest {
    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private TestCenterRepository testCenterRepository;

    @Autowired
    private CandidateEvaluationGridRepository candidateEvaluationGridRepository;

    @Test
    void testFindAllByTestCenterEntityCode(){
        //given
        TestCenterEntity testCenterEntity1 = TestCenterEntity
                .builder()
                .code(TestCenterCode.GRE)
                .build();
        TestCenterEntity testCenterEntity2 = TestCenterEntity
                .builder()
                .code(TestCenterCode.DIJ)
                .build();

        CandidateEntity candidateEntity1 = CandidateEntity
                .builder()
                .firstname("Imen")
                .email("test@gmail.com")
                .testCenterEntity(testCenterEntity1)
                .build();

        CandidateEntity candidateEntity2 = CandidateEntity
                .builder()
                .firstname("Michael")
                .email("toto@gmail.com")
                .testCenterEntity(testCenterEntity2)
                .build();

        testCenterRepository.save(testCenterEntity1);
        testCenterRepository.save(testCenterEntity2);

        candidateRepository.save(candidateEntity1);
        candidateRepository.save(candidateEntity2);

        //when
        Set<CandidateEntity> candidatesEntityResponse =candidateRepository.findAllByTestCenterEntityCode(TestCenterCode.GRE);

        //then
        assertThat(candidatesEntityResponse).hasSize(1);
        assertThat(candidatesEntityResponse.stream().findFirst().get().getTestCenterEntity().getCode()).isEqualTo(TestCenterCode.GRE);

    }

    @Test
    void testFindAllByCandidateEvaluationGridEntitiesGradeLessThan(){
    //given

        CandidateEntity candidateEntity1 = CandidateEntity
                .builder()
                .firstname("Imen")
                .email("test@gmail.fr")
                .build();

        CandidateEntity candidateEntity2 = CandidateEntity
                .builder()
                .firstname("Adrien")
                .email("toto@gmail.com")
                .build();

        candidateRepository.save(candidateEntity1);
        candidateRepository.save(candidateEntity2);

        CandidateEvaluationGridEntity candidateEvaluationGridEntity1 = CandidateEvaluationGridEntity
                .builder()
                        .grade(5)
                .candidateEntity(candidateEntity1)
                                .build();

        CandidateEvaluationGridEntity candidateEvaluationGridEntity2 = CandidateEvaluationGridEntity
                .builder()
                        .grade(15)
                .candidateEntity(candidateEntity2)
                                .build();

        candidateEvaluationGridRepository.save(candidateEvaluationGridEntity1);
        candidateEvaluationGridRepository.save(candidateEvaluationGridEntity2);

        Set<CandidateEvaluationGridEntity> grid1 = new HashSet<>();
        grid1.add(candidateEvaluationGridEntity1);

        Set<CandidateEvaluationGridEntity> grid2 = new HashSet<>();
        grid2.add(candidateEvaluationGridEntity2);


        candidateEntity1.setCandidateEvaluationGridEntities(grid2);
        candidateEntity2.setCandidateEvaluationGridEntities(grid1);

        candidateRepository.save(candidateEntity1);
        candidateRepository.save(candidateEntity2);

        //when
        Set<CandidateEntity> candidatesEntityResponse = candidateRepository.findAllByCandidateEvaluationGridEntitiesGradeLessThan(10);

        //then
        assertThat(candidatesEntityResponse).hasSize(1);
        assertThat(candidatesEntityResponse.stream().findFirst().get().getCandidateEvaluationGridEntities().stream().findFirst().get().getGrade()).isEqualTo(5);

    }

    @Test
    void findAllByHasExtraTimeFalseAndBirthDateBefore(){
        //given
        CandidateEntity candidateEntity1 = CandidateEntity
                .builder()
                .firstname("test")
                .hasExtraTime(false)
                .birthDate(LocalDate.of(2002,5,21))
                .email("test1@gmail.com")
                .build();

        CandidateEntity candidateEntity2 = CandidateEntity
                .builder()
                .firstname("Adrien")
                .hasExtraTime(false)
                .birthDate(LocalDate.of(2001,7,5))
                .email("test2@gmail.com")
                .build();

        CandidateEntity candidateEntity3 = CandidateEntity
                .builder()
                .firstname("Imenos")
                .hasExtraTime(true)
                .birthDate(LocalDate.of(2002,5,21))
                .email("test3@gmail.com")
                .build();

        CandidateEntity candidateEntity4 = CandidateEntity
                .builder()
                .firstname("Adrienos")
                .hasExtraTime(true)
                .birthDate(LocalDate.of(2001,7,5))
                .email("test4@gmail.com")
                .build();

        candidateRepository.save(candidateEntity1);
        candidateRepository.save(candidateEntity2);
        candidateRepository.save(candidateEntity3);
        candidateRepository.save(candidateEntity4);

        //when
        Set<CandidateEntity> responsesCandidateEntity = candidateRepository.findAllByHasExtraTimeFalseAndBirthDateBefore(LocalDate.of(2002,1,1));


        //then
        assertThat(responsesCandidateEntity).hasSize(1);
        assertThat(responsesCandidateEntity.stream().findFirst().get().getBirthDate()).isEqualTo(LocalDate.of(2001,7,5));
        assertThat(responsesCandidateEntity.stream().findFirst().get().isHasExtraTime()).isFalse();

    }
}
