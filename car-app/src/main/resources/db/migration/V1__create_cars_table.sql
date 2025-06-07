CREATE TABLE cars (
    id UUID PRIMARY KEY,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    year INTEGER NOT NULL,
    license_plate VARCHAR(255) NOT NULL,
    rental_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    color VARCHAR(255),
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    modified_by VARCHAR(255) NOT NULL
); 