CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(40) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    replaced_by_token_hash VARCHAR(64) NULL,
    device_info VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES app_users(id),
    INDEX idx_refresh_user_active (user_id, revoked_at),
    INDEX idx_refresh_expiry (expires_at)
) ENGINE=InnoDB;

CREATE TABLE vehicles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    registration_number VARCHAR(50) NOT NULL,
    name_model VARCHAR(120) NOT NULL,
    type VARCHAR(30) NOT NULL,
    region VARCHAR(100) NOT NULL,
    max_load_capacity_kg DECIMAL(14,2) NOT NULL,
    odometer_km DECIMAL(16,2) NOT NULL,
    acquisition_cost DECIMAL(16,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_vehicle_registration UNIQUE (registration_number),
    INDEX idx_vehicle_status (status),
    INDEX idx_vehicle_type_region (type, region)
) ENGINE=InnoDB;

CREATE TABLE drivers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    license_number VARCHAR(80) NOT NULL,
    license_category VARCHAR(60) NOT NULL,
    license_expiry_date DATE NOT NULL,
    contact_number VARCHAR(30) NOT NULL,
    email VARCHAR(190) NULL,
    region VARCHAR(100) NOT NULL,
    safety_score DECIMAL(5,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_driver_license UNIQUE (license_number),
    INDEX idx_driver_status (status),
    INDEX idx_driver_license_expiry (license_expiry_date),
    INDEX idx_driver_region (region)
) ENGINE=InnoDB;

CREATE TABLE trips (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source VARCHAR(180) NOT NULL,
    destination VARCHAR(180) NOT NULL,
    vehicle_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    cargo_weight_kg DECIMAL(14,2) NOT NULL,
    planned_distance_km DECIMAL(14,2) NOT NULL,
    actual_distance_km DECIMAL(14,2) NULL,
    start_odometer_km DECIMAL(16,2) NOT NULL,
    final_odometer_km DECIMAL(16,2) NULL,
    fuel_consumed_liters DECIMAL(14,3) NULL,
    revenue DECIMAL(16,2) NULL,
    status VARCHAR(30) NOT NULL,
    dispatched_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    cancelled_at DATETIME(6) NULL,
    notes VARCHAR(1000) NULL,
    cancellation_reason VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_trip_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_trip_driver FOREIGN KEY (driver_id) REFERENCES drivers(id),
    INDEX idx_trip_status (status),
    INDEX idx_trip_vehicle_status (vehicle_id, status),
    INDEX idx_trip_driver_status (driver_id, status),
    INDEX idx_trip_created_at (created_at)
) ENGINE=InnoDB;

CREATE TABLE maintenance_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    service_type VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    cost DECIMAL(16,2) NOT NULL DEFAULT 0,
    odometer_at_service DECIMAL(16,2) NULL,
    status VARCHAR(30) NOT NULL,
    closing_notes VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    INDEX idx_maintenance_vehicle_status (vehicle_id, status),
    INDEX idx_maintenance_start_date (start_date)
) ENGINE=InnoDB;

CREATE TABLE fuel_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    trip_id BIGINT NULL,
    liters DECIMAL(14,3) NOT NULL,
    cost DECIMAL(16,2) NOT NULL,
    log_date DATE NOT NULL,
    odometer_km DECIMAL(16,2) NULL,
    notes VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_fuel_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_fuel_trip FOREIGN KEY (trip_id) REFERENCES trips(id),
    INDEX idx_fuel_vehicle_date (vehicle_id, log_date),
    INDEX idx_fuel_trip (trip_id)
) ENGINE=InnoDB;

CREATE TABLE expenses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    trip_id BIGINT NULL,
    type VARCHAR(40) NOT NULL,
    amount DECIMAL(16,2) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_expense_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_expense_trip FOREIGN KEY (trip_id) REFERENCES trips(id),
    INDEX idx_expense_vehicle_date (vehicle_id, expense_date),
    INDEX idx_expense_type (type),
    INDEX idx_expense_trip (trip_id)
) ENGINE=InnoDB;

CREATE TABLE vehicle_documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    expiry_date DATE NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_vehicle_document_stored_name UNIQUE (stored_file_name),
    CONSTRAINT fk_vehicle_document_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    INDEX idx_vehicle_document_vehicle (vehicle_id),
    INDEX idx_vehicle_document_expiry (expiry_date)
) ENGINE=InnoDB;
