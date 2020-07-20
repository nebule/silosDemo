package com.nebojsa.silos.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
public class SiloDto {

    private Long id;
    private String name;
    private Long capacity;
}
