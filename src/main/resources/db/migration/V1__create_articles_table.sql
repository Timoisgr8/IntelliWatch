CREATE TABLE articles
(
    id               BIGSERIAL PRIMARY KEY,
    title            TEXT        NOT NULL,
    url              TEXT UNIQUE NOT NULL,
    source           VARCHAR(100),
    fetched_at       TIMESTAMP   NOT NULL,
    summary          TEXT,
    risk_tags        TEXT[],
    confidence_score DOUBLE PRECISION
);