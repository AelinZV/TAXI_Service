package com.taxi.notificationservice.repository;

import com.taxi.notificationservice.entity.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationTask, Long> {

    @Query(value = "SELECT * FROM notification_tasks WHERE status = 'PENDING' ORDER BY created_at LIMIT 5", nativeQuery = true)
    List<NotificationTask> findPendingTasks();

    List<NotificationTask> findByTripId(Long tripId);

    @Modifying
    @Query(value = "UPDATE notification_tasks SET status = 'PROCESSING' WHERE id = :id AND status = 'PENDING'", nativeQuery = true)
    int claimTask(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE notification_tasks SET status = :status WHERE id = :id", nativeQuery = true)
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Modifying
    @Query(value = "UPDATE notification_tasks SET attempts = attempts + 1 WHERE id = :id", nativeQuery = true)
    void incrementAttempts(@Param("id") Long id);

    @Query(value = "SELECT attempts FROM notification_tasks WHERE id = :id", nativeQuery = true)
    Integer getAttempts(@Param("id") Long id);
}