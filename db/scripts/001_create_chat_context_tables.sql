CREATE SCHEMA IF NOT EXISTS log_analyser;
USE log_analyser;

CREATE TABLE chat_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content LONGTEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_message_session
        FOREIGN KEY (session_id)
        REFERENCES chat_session(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE chat_analysis_result (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    summary LONGTEXT NOT NULL,
    findings_json LONGTEXT NOT NULL,
    timeline_json LONGTEXT NOT NULL,
    recommendations_json LONGTEXT NOT NULL,
    confidence_score DOUBLE NOT NULL,
    raw_response LONGTEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_result_session
        FOREIGN KEY (session_id)
        REFERENCES chat_session(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_chat_message_session_created
    ON chat_message(session_id, created_at);

CREATE INDEX idx_chat_result_session_created
    ON chat_analysis_result(session_id, created_at);