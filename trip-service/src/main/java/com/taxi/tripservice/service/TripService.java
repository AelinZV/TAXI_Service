package com.taxi.tripservice.service;

import com.taxi.tripservice.entity.Trip;
import com.taxi.tripservice.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j  // ✅ Добавляем логирование
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // ✅ RestTemplate инжектируется через конструктор (см. TripServiceApplication)
    private final RestTemplate restTemplate;

    @Value("${app.tariff-per-km:15.0}")
    private double tariffPerKm;

    @Value("${app.surge-threshold:3}")
    private int surgeThreshold;

    @Value("${app.surge-multiplier:1.5}")
    private double surgeMultiplier;

    @Value("${user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    @Value("${notification-service.url:http://notification-service:8083}")
    private String notificationServiceUrl;  // ✅ Вынесли в конфиг

    public record TripRequest(Long passengerId, String origin, String destination, BigDecimal distanceKm) {}

    @Transactional
    public Trip createTrip(TripRequest request) {
        log.info("Creating trip for passenger {} from {} to {}",
                request.passengerId(), request.origin(), request.destination());

        // ✅ Проверяем существование пассажира с правильной обработкой ошибок
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    userServiceUrl + "/passengers/" + request.passengerId(),
                    HttpMethod.GET,
                    null,
                    Void.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Passenger {} check returned status: {}", request.passengerId(), response.getStatusCode());
            }
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            log.error("Access denied to passenger {}: {}", request.passengerId(), e.getMessage());
            throw new RuntimeException("Access denied to passenger service");
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.warn("Passenger {} not found", request.passengerId());
            throw new RuntimeException("Passenger not found: " + request.passengerId());
        } catch (Exception e) {
            log.error("Failed to verify passenger {}: {}", request.passengerId(), e.getMessage(), e);
            throw new RuntimeException("Service unavailable: " + e.getMessage());
        }

        // ✅ Проверяем доступность водителей
        try {
            Long freeCount = restTemplate.getForObject(
                    userServiceUrl + "/drivers/free/count", Long.class);
            if (freeCount == null || freeCount == 0) {
                log.info("No free drivers available");
                throw new RuntimeException("No free drivers available");
            }
        } catch (Exception e) {
            log.warn("Failed to check free drivers: {}", e.getMessage());
            // Не блокируем создание поездки, если сервис недоступен
        }

        // Создаём поездку
        Trip trip = new Trip();
        trip.setPassengerId(request.passengerId());
        trip.setDriverId(findAvailableDriverId());  // ✅ Вместо заглушки
        trip.setOrigin(request.origin());
        trip.setDestination(request.destination());
        trip.setDistanceKm(request.distanceKm());
        trip.setPrice(calculateDynamicPrice(request.distanceKm()));
        trip.setStatus("ASSIGNED");

        Trip saved = tripRepository.save(trip);
        log.info("Trip {} created successfully", saved.getId());

        // Создаём задачи уведомлений (асинхронно, не блокируем ответ)
        createNotificationTaskAsync(saved.getId(), "DRIVER", trip.getDriverId(), "New trip assigned");
        createNotificationTaskAsync(saved.getId(), "PASSENGER", request.passengerId(), "Driver found");

        return saved;
    }

    // ✅ Метод для поиска доступного водителя (заглушка для демонстрации)
    private Long findAvailableDriverId() {
        // В реальности: атомарный запрос к user-service с FOR UPDATE SKIP LOCKED
        // Для демо: возвращаем первого свободного водителя
        try {
            List<Map> drivers = restTemplate.getForObject(
                    userServiceUrl + "/drivers/free", List.class);
            if (drivers != null && !drivers.isEmpty()) {
                return ((Number) drivers.get(0).get("id")).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch free drivers: {}", e.getMessage());
        }
        return 1L; // Fallback
    }

    public Trip getById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trip {} not found", id);
                    return new RuntimeException("Trip not found: " + id);
                });
    }

    public List<Trip> getByPassenger(Long passengerId) {
        return tripRepository.findByPassengerId(passengerId);
    }

    @Transactional
    public Trip updateStatus(Long tripId, String newStatus) {
        log.info("Updating trip {} status to {}", tripId, newStatus);

        Trip trip = getById(tripId);
        String oldStatus = trip.getStatus();

        if ("COMPLETED".equals(oldStatus) || "CANCELLED".equals(oldStatus)) {
            throw new RuntimeException("Cannot change terminal status");
        }

        trip.setStatus(newStatus);

        if (("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus)) && trip.getDriverId() != null) {
            try {
                restTemplate.patchForObject(
                        userServiceUrl + "/drivers/" + trip.getDriverId() + "/status?status=FREE",
                        null, Void.class);
                invalidateFreeDriversCache();
                log.info("Driver {} freed after trip {}", trip.getDriverId(), tripId);
            } catch (Exception e) {
                log.warn("Failed to free driver {}: {}", trip.getDriverId(), e.getMessage());
            }
        }

        return tripRepository.save(trip);
    }

    @Transactional
    public Trip rateTrip(Long tripId, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be 1-5");
        }
        Trip trip = getById(tripId);
        if (!"COMPLETED".equals(trip.getStatus())) {
            throw new RuntimeException("Can only rate completed trips");
        }
        if (trip.getRating() != null) {
            throw new RuntimeException("Trip already rated");
        }
        trip.setRating(rating);
        log.info("Trip {} rated {}", tripId, rating);
        return tripRepository.save(trip);
    }

    public Map<String, Object> getDailyStats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> stats = new HashMap<>();
        stats.put("trips_today", tripRepository.countTripsToday(today.atStartOfDay()));
        Double avg = tripRepository.avgPriceToday(today.atStartOfDay());
        stats.put("avg_price", avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        stats.put("free_drivers", getFreeDriversCountFromCache());
        return stats;
    }

    private BigDecimal calculateDynamicPrice(BigDecimal km) {
        if (km == null || km.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid distance");
        }
        long free = getFreeDriversCountFromCache();
        double mult = (free < surgeThreshold) ? surgeMultiplier : 1.0;
        BigDecimal basePrice = km.multiply(BigDecimal.valueOf(tariffPerKm));
        return basePrice.multiply(BigDecimal.valueOf(mult)).setScale(2, RoundingMode.HALF_UP);
    }

    public long getFreeDriversCountFromCache() {
        String key = "drivers:free:count";
        Long cached = (Long) redisTemplate.opsForValue().get(key);
        if (cached != null) return cached;

        try {
            Long count = restTemplate.getForObject(
                    userServiceUrl + "/drivers/free/count", Long.class);
            if (count != null) {
                redisTemplate.opsForValue().set(key, count, 2, TimeUnit.MINUTES);
                return count;
            }
        } catch (Exception e) {
            log.debug("Cache miss for free drivers count: {}", e.getMessage());
        }
        return 0;
    }

    private void invalidateFreeDriversCache() {
        redisTemplate.delete("drivers:free:count");
    }

    // ✅ Асинхронное создание уведомления (не блокирует ответ)
    private void createNotificationTaskAsync(Long tripId, String recipientType, Long recipientId, String message) {
        try {
            // Запускаем в отдельном потоке, чтобы не блокировать ответ
            new Thread(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Type", "application/json");

                    Map<String, Object> payload = Map.of(
                            "tripId", tripId,
                            "recipientType", recipientType,
                            "recipientId", recipientId,
                            "message", message
                    );

                    restTemplate.postForEntity(
                            notificationServiceUrl + "/notifications",
                            new HttpEntity<>(payload, headers),
                            Void.class);
                    log.debug("Notification task created for trip {}", tripId);
                } catch (Exception e) {
                    log.warn("Failed to create notification for trip {}: {}", tripId, e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            log.warn("Failed to schedule notification task: {}", e.getMessage());
        }
    }
}