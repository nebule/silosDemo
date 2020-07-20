package com.nebojsa.silos.dto;

import com.nebojsa.silos.entity.Silo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SiloMapper {

    SiloDto toDto(Silo silo);

    Silo toEntity(SiloDto siloDto);

}
