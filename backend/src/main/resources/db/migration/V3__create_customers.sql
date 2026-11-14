
CREATE TABLE customers (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           company_id UUID NOT NULL,

                           name VARCHAR(160) NOT NULL,

                           email VARCHAR(255),

                           phone VARCHAR(30),

                           document VARCHAR(18),

                           address TEXT,

                           active BOOLEAN NOT NULL DEFAULT TRUE,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_customers_company
                               FOREIGN KEY (company_id)
                                   REFERENCES companies(id),

                           CONSTRAINT uq_customers_company_document
                               UNIQUE (company_id, document),

                           CONSTRAINT ck_customers_name
                               CHECK (LENGTH(TRIM(name)) > 0)
);

CREATE INDEX idx_customers_company_id
    ON customers (company_id);
