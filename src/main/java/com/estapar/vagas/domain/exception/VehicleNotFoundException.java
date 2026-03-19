package com.estapar.vagas.domain.exception;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(String licensePlate) {
        super("No active session found for plate: " + licensePlate);
    }
}
