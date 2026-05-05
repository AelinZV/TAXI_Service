package com.taxi.userservice.service;

import com.taxi.userservice.entity.Passenger;
import com.taxi.userservice.repository.PassengerRepository;
import com.taxi.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public Map<String, Object> register(Passenger passenger) {
        if (passengerRepository.existsByEmail(passenger.getEmail())) {
            throw new RuntimeException("Passenger with this email already exists");
        }
        passenger.setBalance(BigDecimal.valueOf(1000.0));
        Passenger saved = passengerRepository.save(passenger);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getId(), "PASSENGER");
        return Map.of(
                "id", saved.getId(),
                "name", saved.getName(),
                "email", saved.getEmail(),
                "balance", saved.getBalance(),
                "token", token
        );
    }

    public Passenger getById(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Passenger not found: " + id));
    }

    @Transactional
    public Passenger topUpBalance(Long id, BigDecimal amount) {
        Passenger passenger = getById(id);
        passenger.setBalance(passenger.getBalance().add(amount));
        return passengerRepository.save(passenger);
    }
}