package com.taxi.notificationservice.controller;

import com.taxi.notificationservice.entity.NotificationTask;
import com.taxi.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;

    @PostMapping
    public ResponseEntity<NotificationTask> create(@RequestBody NotificationTask task) {
        return ResponseEntity.ok(repository.save(task));
    }

    @GetMapping
    public ResponseEntity<List<NotificationTask>> getByTrip(@RequestParam(required = false) Long trip_id) {
        if (trip_id != null) {
            return ResponseEntity.ok(repository.findByTripId(trip_id));
        }
        return ResponseEntity.ok(repository.findAll());
    }
}