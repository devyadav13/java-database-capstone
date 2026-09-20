package com.smartclinic.service;

import com.smartclinic.model.Doctor;
import com.smartclinic.repository.AdminRepository;
import com.smartclinic.repository.DoctorRepository;
import com.smartclinic.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private DoctorRepository doctorRepository;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        doctorRepository = Mockito.mock(DoctorRepository.class);
        tokenService = new TokenService(Mockito.mock(AdminRepository.class), doctorRepository, Mockito.mock(PatientRepository.class));
        ReflectionTestUtils.setField(tokenService, "jwtSecret", "unit-test-secret-key-that-is-at-least-32-chars");
        ReflectionTestUtils.setField(tokenService, "expirationDays", 1L);
    }

    @Test
    void generatedTokenContainsEmailAsSubject() {
        String token = tokenService.generateToken("doc@clinic.com");
        assertEquals("doc@clinic.com", tokenService.extractIdentifier(token));
    }

    @Test
    void signingKeyIsBuiltFromSecret() {
        assertNotNull(tokenService.getSigningKey());
    }

    @Test
    void validTokenForExistingDoctorIsAccepted() {
        Mockito.when(doctorRepository.findByEmail("doc@clinic.com")).thenReturn(new Doctor());
        String token = tokenService.generateToken("doc@clinic.com");
        assertTrue(tokenService.validateToken(token, "doctor"));
    }

    @Test
    void tokenIsRejectedForWrongRoleOrGarbage() {
        String token = tokenService.generateToken("doc@clinic.com");
        assertFalse(tokenService.validateToken(token, "admin"));
        assertFalse(tokenService.validateToken("not-a-token", "doctor"));
    }
}
