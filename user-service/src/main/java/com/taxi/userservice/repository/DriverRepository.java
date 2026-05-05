package com.taxi.userservice.repository;

import com.taxi.userservice.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmail(String email);
    List<Driver> findByStatus(String status);

    @Query(value = "SELECT * FROM drivers WHERE status = 'FREE' ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<Driver> findNextFreeDriver();

    long countByStatus(String status);
}