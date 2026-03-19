package com.estapar.vagas.domain.exception;

public class SpotNotFoundException extends RuntimeException {

    public SpotNotFoundException(double lat, double lng) {
        super("No spot found at coordinates: lat=" + lat + ", lng=" + lng);
    }
}
