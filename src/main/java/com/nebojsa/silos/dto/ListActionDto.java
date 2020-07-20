package com.nebojsa.silos.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListActionDto {

    private List<ActionDto> actions = new ArrayList<>();

}
