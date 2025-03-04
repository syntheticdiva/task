-- Создание пользовательских ENUM типов
CREATE TYPE role_type AS ENUM ('USER', 'ADMIN');
CREATE TYPE task_status_type AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE task_priority_type AS ENUM ('HIGH', 'MEDIUM', 'LOW');

-- Создание таблицы users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role role_type NOT NULL
);

-- Создание таблицы tasks
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status task_status_type NOT NULL,
    priority task_priority_type NOT NULL,
    author_id BIGINT NOT NULL REFERENCES users(id),
    assignee_id BIGINT REFERENCES users(id)
);

-- Создание таблицы comments
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id)
);