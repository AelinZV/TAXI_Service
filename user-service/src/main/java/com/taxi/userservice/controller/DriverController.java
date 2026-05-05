package com.taxi.userservice.controller;

import com.taxi.userservice.entity.Driver;
import com.taxi.userservice.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.getById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Driver> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(driverService.updateStatus(id, status));
    }

    @GetMapping("/free")
    public ResponseEntity<Map<String, Object>> getFreeDrivers() {
        List<Driver> drivers = driverService.getFreeDrivers();
        return ResponseEntity.ok(Map.of(
                "count", drivers.size(),
                "drivers", drivers
        ));
    }

    @GetMapping("/free/count")
    public ResponseEntity<Map<String, Long>> getFreeDriversCount() {
        long count = driverService.getFreeDriversCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
}