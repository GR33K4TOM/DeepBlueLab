package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TreatmentController.class)
@Import(GlobalExceptionHandler.class)
class TreatmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TreatmentService service;

    @Test
    void shouldCreateTreatment() throws Exception {
        TreatmentResponse response = new TreatmentResponse(
                100L,
                "AN-2026-001",
                "SPEC-001",
                LocalDateTime.of(2026, 8, 21, 9, 30),
                TreatmentType.WOUND_CARE,
                "Cleaning and treatment of flipper injury."
        );

        when(service.register(any(CreateTreatmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "animalCode": "AN-001",
                            "specialistCode": "SPEC-001",
                            "performedAt": "2026-08-21T09:00:00",
                            "type": "WOUND_CARE",
                            "description": "Cleaning and treatment of flipper injury."
                        }
                        """))
                .andExpect(status().isCreated());

        verify(service).register(any(CreateTreatmentRequest.class));
    }

    @Test
    void shouldReturn400WhenPostRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "animalCode": "",
                            "specialistCode": "",
                            "type": null,
                            "description": ""
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(service, never()).register(any());
    }
}
