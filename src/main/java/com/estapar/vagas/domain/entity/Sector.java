package com.estapar.vagas.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sector")
@Getter
@Setter
@NoArgsConstructor
public class Sector {

    @Id
    @Column(name = "name", length = 10, nullable = false)
    private String name;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "current_occupancy", nullable = false)
    private int currentOccupancy = 0;

    public Sector(String name, BigDecimal basePrice, int maxCapacity) {
        this.name = name;
        this.basePrice = basePrice;
        this.maxCapacity = maxCapacity;
        this.currentOccupancy = 0;
    }

    public boolean isFull() {
        return currentOccupancy >= maxCapacity;
    }

    public void incrementOccupancy() {
        this.currentOccupancy++;
    }

    public void decrementOccupancy() {
        if (this.currentOccupancy > 0) {
            this.currentOccupancy--;
        }
    }
}
