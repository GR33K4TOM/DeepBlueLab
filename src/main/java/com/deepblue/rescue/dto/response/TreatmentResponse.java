package com.deepblue.rescue.dto.response;

import java.time.LocalDateTime;
import com.deepblue.rescue.domain.TreatmentType;

public record TreatmentResponse(

       
    Long id,
    String animalCode,
    String specialistCode,
    LocalDateTime performedAt,
    TreatmentType type,
    String description
) {
}
