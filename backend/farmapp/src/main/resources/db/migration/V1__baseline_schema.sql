CREATE TABLE users (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    email_confirmed BOOLEAN NOT NULL DEFAULT TRUE,
    email_confirmation_token_hash VARCHAR(255),
    email_confirmation_token_expires_at TIMESTAMP(6) WITH TIME ZONE,
    avatar_url TEXT,
    plan VARCHAR(255) NOT NULL DEFAULT 'FREE',
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_users_email ON users (email);

CREATE TABLE farms (
    id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    owner_id UUID NOT NULL,
    CONSTRAINT pk_farms PRIMARY KEY (id)
);

CREATE TABLE animals (
    id VARCHAR(255) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    breed VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    status VARCHAR(255) NOT NULL,
    origin VARCHAR(255) NOT NULL,
    acquisition_cost DOUBLE PRECISION,
    sale_price DOUBLE PRECISION,
    sale_date DATE,
    farm_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_animals PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_animals_tag ON animals (tag);

CREATE TABLE productions (
    id VARCHAR(255) NOT NULL,
    animal_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    farm_id VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    CONSTRAINT pk_productions PRIMARY KEY (id)
);

CREATE TABLE feedings (
    id VARCHAR(255) NOT NULL,
    animal_id VARCHAR(255) NOT NULL,
    feed_type_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    farm_id VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    CONSTRAINT pk_feedings PRIMARY KEY (id)
);

CREATE TABLE feed_types (
    id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    cost_per_kg DOUBLE PRECISION NOT NULL,
    active BOOLEAN NOT NULL,
    farm_id VARCHAR(255),
    CONSTRAINT pk_feed_types PRIMARY KEY (id)
);

CREATE TABLE milk_prices (
    id VARCHAR(255) NOT NULL,
    farm_id VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    effective_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    CONSTRAINT pk_milk_prices PRIMARY KEY (id)
);

CREATE TABLE user_farm_assignments (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    farm_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user_farm_assignments PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_user_farm_assignments_user_farm
    ON user_farm_assignments (user_id, farm_id);

CREATE TABLE animal_batches (
    id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    farm_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_animal_batches PRIMARY KEY (id)
);

CREATE TABLE animal_batch_members (
    id VARCHAR(255) NOT NULL,
    batch_id VARCHAR(255) NOT NULL,
    animal_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_animal_batch_members PRIMARY KEY (id)
);
