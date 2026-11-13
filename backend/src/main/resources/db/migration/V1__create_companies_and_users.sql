
CREATE TABLE companies (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           name VARCHAR(160) NOT NULL,
                           slug VARCHAR(80) NOT NULL UNIQUE,
                           active BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       company_id UUID NOT NULL,
                       name VARCHAR(160) NOT NULL,
                       email VARCHAR(254) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_users_company
                           FOREIGN KEY (company_id)
                               REFERENCES companies(id),

                       CONSTRAINT ck_users_role
                           CHECK (role IN ('ADMIN', 'OPERATOR')),

                       CONSTRAINT uq_users_company_id
                           UNIQUE (company_id, id)
);

CREATE UNIQUE INDEX uq_users_company_email
    ON users (company_id, LOWER(email));
