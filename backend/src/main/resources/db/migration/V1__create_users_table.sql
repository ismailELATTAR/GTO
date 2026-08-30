-- id is the Keycloak user's "sub" claim, not generated here — this table
-- mirrors the Keycloak account, it doesn't own identity/credentials.
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    display_name    VARCHAR(100) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_login_at   TIMESTAMPTZ
);

CREATE TABLE user_profiles (
    user_id     UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    avatar_url  VARCHAR(500),
    bio         VARCHAR(500),
    timezone    VARCHAR(50)
);
