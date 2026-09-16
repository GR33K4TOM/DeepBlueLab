package com.deepblue.rescue.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.dto.response.RescueCaseResponse;

@Mapper(componentModel = "spring")
public interface RescueCaseMapper {

    @Mapping(
        target = "centerCode",
        source = "rescueCenter.code"
    )
    @Mapping(
        target = "animalCode",
        source = "animal.animalCode"
    )
    RescueCaseResponse toResponse(
            RescueCase rescueCase
    );
}
