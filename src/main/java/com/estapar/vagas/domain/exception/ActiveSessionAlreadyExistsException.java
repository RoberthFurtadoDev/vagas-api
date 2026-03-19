package com.estapar.vagas.domain.exception;

public class ActiveSessionAlreadyExistsException extends RuntimeException {

    public ActiveSessionAlreadyExistsException(String licensePlate) {
        super("An active session already exists for plate: " + licensePlate);
    }
}
