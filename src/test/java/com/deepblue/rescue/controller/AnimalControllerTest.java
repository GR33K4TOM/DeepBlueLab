package com.deepblue.rescue.controller;

import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.service.AnimalService;
import com.deepblue.rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnimalController.class)
@Import(GlobalExceptionHandler.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @MockitoBean
    private TreatmentService treatmentService;

    @Test
    void shouldReturnAnimalByCode() throws Exception {
        mockMvc.perform(get("/api/animals/{animalCode}", "AN-001"))
                .andExpect(status().isOk());

        verify(animalService).findByCode("AN-001");
    }

    @Test
    void shouldReturnAnimalsInRehabilitation() throws Exception {
        when(animalService.findAnimalsInRehabilitation())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/animals/in-rehabilitation"))
                .andExpect(status().isOk());

        verify(animalService).findAnimalsInRehabilitation();
    }

    @Test
    void shouldReturnAnimalTreatments() throws Exception {
        mockMvc.perform(get("/api/animals/{animalCode}/treatments", "AN-001"))
                .andExpect(status().isOk());

        verify(treatmentService).findByAnimalCode("AN-001");
    }

    @Test
    void shouldReturnTreatmentEligibility() throws Exception {
        when(animalService.canReceiveTreatment("AN-001")).thenReturn(true);

        mockMvc.perform(get("/api/animals/{animalCode}/treatment-eligibility", "AN-001"))
                .andExpect(status().isOk());

        verify(animalService).canReceiveTreatment("AN-001");
    }
}
