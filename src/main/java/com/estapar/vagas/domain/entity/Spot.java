package com.estapar.vagas.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "spot", indexes = {
        @Index(name = "idx_spot_sector", columnList = "sector_name"),
        @Index(name = "idx_spot_lat_lng", columnList = "lat, lng")
})
@Getter
@Setter
@NoArgsConstructor
public class Spot {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_name", nullable = false)
    private Sector sector;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lng", nullable = false)
    private double lng;

    @Column(name = "occupied", nullable = false)
    private boolean occupied = false;

    public Spot(Long id, Sector sector, double lat, double lng) {
        this.id = id;
        this.sector = sector;
        this.lat = lat;
        this.lng = lng;
        this.occupied = false;
    }
}
