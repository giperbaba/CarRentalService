CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    car_id UUID NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    actual_end_date TIMESTAMP,
    total_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL
);