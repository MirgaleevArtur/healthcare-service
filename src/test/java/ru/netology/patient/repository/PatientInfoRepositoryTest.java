package ru.netology.patient.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


class PatientInfoRepositoryTest {

    @TempDir
    Path tempDir;

    private PatientInfo testPatient;
    private ObjectMapper mapper;
    private PatientInfoRepository patientInfoRepository;
    private File repoFile;


    @BeforeEach
    void setUp() {

        String testPatientId = UUID.randomUUID().toString();
        HealthInfo healthInfo = new HealthInfo(
                new BigDecimal("36.6"),
                new BloodPressure(120, 80)
        );

        testPatient = new PatientInfo(
                testPatientId,
                "Oleg",
                "Olegov",
                LocalDate.of(1982, 1, 16),
                healthInfo
        );

        mapper = new ObjectMapper();
        mapper.registerModules(new JavaTimeModule(), new ParameterNamesModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        repoFile = tempDir.resolve("patients_temp.txt").toFile();
        patientInfoRepository = new PatientInfoFileRepository(repoFile, mapper);
    }

    @Test
    void shouldBePatientInfo() throws IOException {
        mapper.writeValue(repoFile, testPatient);
        PatientInfo found = patientInfoRepository.getById(testPatient.getId());
        assertEquals(testPatient, found);
    }

    @Test
    void shouldBeSuccessAdd() throws IOException {
        String newId = patientInfoRepository.add(testPatient);
        PatientInfo savedPatient = mapper.readValue(repoFile, PatientInfo.class);
        assertEquals(newId, savedPatient.getId());
    }

    @Test
    void shouldBeExceptionOnRemove() {
        assertThatThrownBy(() -> patientInfoRepository.remove(testPatient.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not implemented");
    }

    @Test
    void shouldBeExceptionOnUpdate() {
        assertThatThrownBy(() -> patientInfoRepository.update(testPatient))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not implemented");
    }
}