-- passengers: пассажиры
CREATE TABLE IF NOT EXISTS passengers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    balance DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- drivers: водители
CREATE TABLE IF NOT EXISTS drivers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'FREE',
    rating DECIMAL(2, 1) DEFAULT 5.0,
    total_rides INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- trips: поездки
CREATE TABLE IF NOT EXISTS trips (
    id BIGSERIAL PRIMARY KEY,
    passenger_id BIGINT NOT NULL REFERENCES passengers(id),
    driver_id BIGINT REFERENCES drivers(id),
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    origin VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2),
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    distance_km DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- notification_tasks: задачи уведомлений
CREATE TABLE IF NOT EXISTS notification_tasks (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES trips(id),
    recipient_type VARCHAR(20) NOT NULL CHECK (recipient_type IN ('PASSENGER', 'DRIVER')),
    recipient_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_trips_passenger_id ON trips(passenger_id);
CREATE INDEX IF NOT EXISTS idx_trips_driver_id ON trips(driver_id);
CREATE INDEX IF NOT EXISTS idx_trips_status ON trips(status);
CREATE INDEX IF NOT EXISTS idx_trips_created_at ON trips(created_at);
CREATE INDEX IF NOT EXISTS idx_drivers_status ON drivers(status);
CREATE INDEX IF NOT EXISTS idx_notification_tasks_status ON notification_tasks(status);
CREATE INDEX IF NOT EXISTS idx_notification_tasks_trip_id ON notification_tasks(trip_id);