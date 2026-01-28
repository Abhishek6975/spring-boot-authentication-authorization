package com.koyta.auth.exceptions;

public class ContractValidationException extends RuntimeException {
    public ContractValidationException(String message) {
        super(message);
    }
}
