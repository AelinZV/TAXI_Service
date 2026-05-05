package com.taxi.tripservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
public class Driver {
    @Id
    private Long id;
    private String status; // FREE, BUSY
}