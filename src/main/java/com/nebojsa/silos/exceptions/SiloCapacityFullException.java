package com.nebojsa.silos.exceptions;

public class SiloCapacityFullException extends RuntimeException {

    public SiloCapacityFullException(String message) {
        super(message);
    }
}
