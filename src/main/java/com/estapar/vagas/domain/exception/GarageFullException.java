package com.estapar.vagas.domain.exception;

public class GarageFullException extends RuntimeException {

    public GarageFullException() {
        super("Garage is at full capacity. No new entries allowed.");
    }
}
