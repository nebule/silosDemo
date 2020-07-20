package com.nebojsa.silos.dto;


import com.nebojsa.silos.constants.ActionName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ActionDto {

    private Long id;
    private Long amount;
    private Long siloToId;
    private Long siloFromId;
    private Long updatedInMillis;
    private ActionName actionName;

}
