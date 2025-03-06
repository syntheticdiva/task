CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    roles role_type NOT NULL,
    PRIMARY KEY (user_id, roles)
);