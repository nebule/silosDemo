package com.nebojsa.silos.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
public class LevelDto {

    private Long siloId;
    private Long level;
    private Long measureTimestamp;
}
