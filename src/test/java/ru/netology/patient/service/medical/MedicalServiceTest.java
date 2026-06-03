package ru.netology.patient.service.medical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;




@ExtendWith(MockitoExtension.class)
class MedicalServiceTest {

    @Mock
    private PatientInfoRepository patientInfoRepository;

    @Mock
    private SendAlertService alertService;

    @InjectMocks
    private MedicalServiceImpl medicalService;

    private String testPatientId;
    private PatientInfo testPatient;

    @BeforeEach
    void setUp(){
        testPatientId = UUID.randomUUID().toString();
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
    }

    @Test
    void shouldNotBeAlert_whenBloodPressureIsOk() {
         when(patientInfoRepository.getById(testPatientId)).thenReturn(testPatient);

         medicalService.checkBloodPressure(testPatientId, new BloodPressure(120, 80));

        verify(alertService, never()).send(anyString());
    }

    @Test
    void shouldBeAlert_whenBloodPressureIsAbNormal() {
        when(patientInfoRepository.getById(testPatientId)).thenReturn(testPatient);

        medicalService.checkBloodPressure(testPatientId, new BloodPressure(80, 120));

        String expectedMessage = "Warning, patient with id: " + testPatientId + ", need help";
        verify(alertService, times(1)).send(expectedMessage);
    }

    @Test
    void shouldNotBeAlert_whenTemperatureIsNormal() {
        when(patientInfoRepository.getById(testPatientId)).thenReturn(testPatient);
        BigDecimal normalTemperature = new BigDecimal("36.6");

        medicalService.checkTemperature(testPatientId, normalTemperature);

        verify(alertService, never()).send(anyString());
    }

    @Test
    void shouldBeAlert_whenTemperatureIsLow() {
        when(patientInfoRepository.getById(testPatientId)).thenReturn(testPatient);
        BigDecimal normalTemperature = new BigDecimal("26.6");

        medicalService.checkTemperature(testPatientId, normalTemperature);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(alertService, times(1)).send(messageCaptor.capture());

        String actualMessage = messageCaptor.getValue();
        assertTrue(actualMessage.contains("Warning"));
        assertTrue(actualMessage.contains(testPatientId));

    }
}