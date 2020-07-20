package com.nebojsa.silos.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorMessageDto {

    private HttpStatus status;
    private String message;

}
