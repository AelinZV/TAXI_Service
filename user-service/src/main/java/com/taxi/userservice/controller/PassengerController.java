package com.taxi.userservice.controller;

import com.taxi.userservice.entity.Passenger;
import com.taxi.userservice.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @GetMapping("/{id}")
    public ResponseEntity<Passenger> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.getById(id));
    }

    @PatchMapping("/{id}/topup")
    public ResponseEntity<Passenger> topUp(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(passengerService.topUpBalance(id, amount));
    }
}