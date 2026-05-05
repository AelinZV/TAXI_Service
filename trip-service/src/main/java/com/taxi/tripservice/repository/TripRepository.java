package com.taxi.tripservice.repository;

import com.taxi.tripservice.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByPassengerId(Long passengerId);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.createdAt >= :date")
    long countTripsToday(@Param("date") LocalDateTime date);

    @Query("SELECT AVG(t.price) FROM Trip t WHERE t.createdAt >= :date")
    Double avgPriceToday(@Param("date") LocalDateTime date);
}