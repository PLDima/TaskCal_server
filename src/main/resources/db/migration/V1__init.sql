-- Kept compatible with both PostgreSQL and H2 in PostgreSQL mode (used by tests).

CREATE TABLE users (
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- A user's credentials, separate from the user so that Google login can be added later as
-- another provider pointing at the same user_id. For provider 'device', subject is the
-- SHA-256 hex of the device token; the raw token is never stored.
CREATE TABLE auth_identities (
    provider   VARCHAR(32)  NOT NULL,
    subject    VARCHAR(255) NOT NULL,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (provider, subject)
);
CREATE INDEX idx_auth_identities_user ON auth_identities (user_id);

CREATE TABLE user_profiles (
    user_id              UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    name                 VARCHAR(100)     NOT NULL,
    gender               VARCHAR(16),
    date_of_birth        DATE,
    height_cm            INTEGER,
    steps_goal           INTEGER          NOT NULL,
    distance_goal_meters INTEGER          NOT NULL,
    calories_burned_goal INTEGER          NOT NULL,
    weight_goal          DOUBLE PRECISION NOT NULL,
    nutrition_calories   INTEGER          NOT NULL,
    nutrition_protein    DOUBLE PRECISION NOT NULL,
    nutrition_carbs      DOUBLE PRECISION NOT NULL,
    nutrition_fat        DOUBLE PRECISION NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Date-times are zone-less (LocalDateTime), matching how the Android app stores them.
CREATE TABLE tasks (
    id                  UUID PRIMARY KEY,
    user_id             UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title               VARCHAR(200)  NOT NULL,
    description         VARCHAR(5000) NOT NULL,
    priority            VARCHAR(16)   NOT NULL,
    recurrence_type     VARCHAR(16)   NOT NULL,
    repeat_day_of_week  VARCHAR(16),
    repeat_day_of_month INTEGER,
    assigned_date_time  TIMESTAMP,
    has_due_time        BOOLEAN       NOT NULL,
    is_completed        BOOLEAN       NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tasks_user ON tasks (user_id);

CREATE TABLE task_completions (
    id              UUID PRIMARY KEY,
    task_id         UUID      NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    user_id         UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    occurrence_date TIMESTAMP NOT NULL,
    completed_at    TIMESTAMP NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_task_completions_occurrence UNIQUE (task_id, occurrence_date)
);
CREATE INDEX idx_task_completions_user ON task_completions (user_id);

CREATE TABLE food_entries (
    id         UUID PRIMARY KEY,
    user_id    UUID             NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    entry_date DATE             NOT NULL,
    meal_type  VARCHAR(16)      NOT NULL,
    calories   INTEGER          NOT NULL,
    protein    DOUBLE PRECISION NOT NULL,
    carbs      DOUBLE PRECISION NOT NULL,
    fat        DOUBLE PRECISION NOT NULL,
    name       VARCHAR(200)     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_food_entries_user_date ON food_entries (user_id, entry_date);
