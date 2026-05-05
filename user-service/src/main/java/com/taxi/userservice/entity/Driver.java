package com.taxi.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "license_number", unique = true, nullable = false)
    private String licenseNumber;

    @Column(nullable = false)
    private String status = "FREE";

    @Column(precision = 2, scale = 1)
    private BigDecimal rating = BigDecimal.valueOf(5.0);

    @Column(name = "total_rides")
    private Integer totalRides = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}