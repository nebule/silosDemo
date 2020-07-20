package com.nebojsa.silos.exceptions;

public class SiloNotEnoughLevelException extends RuntimeException {

    public SiloNotEnoughLevelException(String message) {
        super(message);
    }
}
