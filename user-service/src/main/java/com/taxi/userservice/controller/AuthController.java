package com.taxi.userservice.controller;

import com.taxi.userservice.entity.Driver;
import com.taxi.userservice.entity.Passenger;
import com.taxi.userservice.service.DriverService;
import com.taxi.userservice.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PassengerService passengerService;
    private final DriverService driverService;

    @PostMapping("/register/passenger")
    public ResponseEntity<?> registerPassenger(@RequestBody Passenger passenger) {
        try {
            return ResponseEntity.ok(passengerService.register(passenger));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/driver")
    public ResponseEntity<?> registerDriver(@RequestBody Driver driver) {
        try {
            return ResponseEntity.ok(driverService.register(driver));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}