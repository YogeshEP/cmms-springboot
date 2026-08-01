-- ==========================================================
-- CMMS Database Setup Script (PostgreSQL)
-- Run this once to create the database, OR simply let
-- spring.jpa.hibernate.ddl-auto=update create the tables for you.
-- ==========================================================

-- Run as a superuser (e.g. psql -U postgres):
-- CREATE DATABASE cmms_db;

-- Below is illustrative of what Hibernate will auto-generate.
-- You do NOT need to run this manually unless you prefer explicit schema control.

CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS technicians (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    specialization VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS assets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    asset_code VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(100),
    manufacturer VARCHAR(255),
    model VARCHAR(255),
    serial_number VARCHAR(255),
    purchase_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'OPERATIONAL',
    location_id BIGINT REFERENCES locations(id)
);

CREATE TABLE IF NOT EXISTS work_orders (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    technician_id BIGINT REFERENCES technicians(id),
    type VARCHAR(50) NOT NULL DEFAULT 'CORRECTIVE',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    due_date DATE,
    completed_date TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS maintenance_schedules (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    frequency VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS spare_parts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    part_number VARCHAR(100) UNIQUE NOT NULL,
    quantity_in_stock INTEGER DEFAULT 0,
    reorder_level INTEGER DEFAULT 5,
    unit_cost DOUBLE PRECISION
);

-- ==========================================================
-- Sample seed data (optional)
-- ==========================================================

INSERT INTO locations (name, description) VALUES
('Plant A - Building 1', 'Main manufacturing plant'),
('Plant B - Warehouse', 'Storage and logistics facility')
ON CONFLICT DO NOTHING;

INSERT INTO technicians (name, email, phone, specialization) VALUES
('John Smith', 'john.smith@cmms.com', '555-0101', 'Electrical'),
('Maria Garcia', 'maria.garcia@cmms.com', '555-0102', 'Mechanical')
ON CONFLICT DO NOTHING;

INSERT INTO assets (name, asset_code, category, manufacturer, model, serial_number, purchase_date, status, location_id) VALUES
('CNC Milling Machine', 'AST-001', 'Machinery', 'Haas', 'VF-2', 'SN-12345', '2022-01-15', 'OPERATIONAL', 1),
('Air Compressor', 'AST-002', 'Utility', 'Atlas Copco', 'GA-15', 'SN-67890', '2021-06-10', 'OPERATIONAL', 1)
ON CONFLICT DO NOTHING;
