package com.taxi.userservice.service;

import com.taxi.userservice.entity.Driver;
import com.taxi.userservice.repository.DriverRepository;
import com.taxi.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    @Transactional
    public Map<String, Object> register(Driver driver) {
        driver.setStatus("FREE");
        Driver saved = driverRepository.save(driver);
        invalidateFreeDriversCache();
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getId(), "DRIVER");
        return Map.of(
                "id", saved.getId(),
                "name", saved.getName(),
                "email", saved.getEmail(),
                "status", saved.getStatus(),
                "token", token
        );
    }

    public Driver getById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + id));
    }

    @Transactional
    public Driver updateStatus(Long id, String status) {
        Driver driver = getById(id);
        String oldStatus = driver.getStatus();
        driver.setStatus(status);
        Driver updated = driverRepository.save(driver);
        if (!oldStatus.equals(status) && ("FREE".equals(status) || "FREE".equals(oldStatus))) {
            invalidateFreeDriversCache();
        }
        return updated;
    }

    public List<Driver> getFreeDrivers() {
        return driverRepository.findByStatus("FREE");
    }

    public long getFreeDriversCount() {
        String cacheKey = "drivers:free:count";
        Long cached = (Long) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;
        long count = driverRepository.countByStatus("FREE");
        redisTemplate.opsForValue().set(cacheKey, count, 2, TimeUnit.MINUTES);
        return count;
    }

    public void invalidateFreeDriversCache() {
        redisTemplate.delete("drivers:free:count");
    }
}