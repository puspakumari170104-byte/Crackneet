CREATE TABLE IF NOT EXISTS users(
 id BIGSERIAL PRIMARY KEY,
 name TEXT NOT NULL,
 email TEXT UNIQUE NOT NULL,
 password_hash TEXT,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS questions(
 id TEXT PRIMARY KEY,
 exam TEXT NOT NULL,
 subject TEXT NOT NULL,
 chapter TEXT,
 type TEXT NOT NULL,
 difficulty TEXT,
 question TEXT NOT NULL,
 options JSONB NOT NULL,
 answer_index INT NOT NULL,
 solution TEXT
);
CREATE INDEX IF NOT EXISTS idx_questions_filters ON questions(exam,subject,type);
CREATE TABLE IF NOT EXISTS test_attempts(
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
 exam TEXT NOT NULL,
 score INT NOT NULL DEFAULT 0,
 total INT NOT NULL,
 correct INT NOT NULL DEFAULT 0,
 wrong INT NOT NULL DEFAULT 0,
 skipped INT NOT NULL DEFAULT 0,
 duration_seconds INT NOT NULL DEFAULT 0,
 answers JSONB,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_attempts_user ON test_attempts(user_id,created_at DESC);
