ALTER TABLE users ADD COLUMN stripe_customer_id VARCHAR(255);
ALTER TABLE users ADD COLUMN stripe_subscription_id VARCHAR(255);
ALTER TABLE users ADD COLUMN billing_subscription_status VARCHAR(64);
ALTER TABLE users ADD COLUMN stripe_cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN stripe_current_period_end TIMESTAMP(6) WITH TIME ZONE;

CREATE UNIQUE INDEX uk_users_stripe_customer_id
    ON users (stripe_customer_id);

CREATE UNIQUE INDEX uk_users_stripe_subscription_id
    ON users (stripe_subscription_id);

CREATE TABLE processed_stripe_events (
    id VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_processed_stripe_events PRIMARY KEY (id)
);
