
CREATE TABLE products (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          company_id UUID NOT NULL,

                          name VARCHAR(160) NOT NULL,

                          sku VARCHAR(80) NOT NULL,

                          description TEXT,

                          price NUMERIC(12, 2) NOT NULL,

                          stock_quantity INTEGER NOT NULL DEFAULT 0,

                          active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_products_company
                              FOREIGN KEY (company_id)
                                  REFERENCES companies(id),

                          CONSTRAINT uq_products_company_sku
                              UNIQUE (company_id, sku),

                          CONSTRAINT ck_products_price
                              CHECK (price >= 0),

                          CONSTRAINT ck_products_stock
                              CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_products_company_id
    ON products (company_id);
