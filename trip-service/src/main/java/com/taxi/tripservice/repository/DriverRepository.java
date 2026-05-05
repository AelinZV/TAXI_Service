package com.taxi.tripservice.repository;

import com.taxi.tripservice.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Query(value = "SELECT * FROM drivers WHERE status = 'FREE' ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<Driver> findAndLockNextFreeDriver();

    @Query(value = "SELECT COUNT(*) FROM drivers WHERE status = 'FREE'", nativeQuery = true)
    long countByStatus(String status);
}