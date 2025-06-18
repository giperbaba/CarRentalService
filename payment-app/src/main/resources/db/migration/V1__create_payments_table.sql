CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
); 