package com.nebojsa.silos.dto;

import com.nebojsa.silos.entity.Action;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;


@Mapper(componentModel = "spring")
public interface ActionMapper {

    @Mappings({
            @Mapping(target = "siloFromId", source = "action.from.id"),
            @Mapping(target = "siloToId", source = "action.to.id")
    })
    ActionDto toDto(Action action);


}
