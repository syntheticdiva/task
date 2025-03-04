CREATE TYPE task_status_type AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE task_priority_type AS ENUM ('HIGH', 'MEDIUM', 'LOW');

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status task_status_type NOT NULL,
    priority task_priority_type NOT NULL,
    author_id BIGINT NOT NULL REFERENCES users(id),
    assignee_id BIGINT REFERENCES users(id)
);