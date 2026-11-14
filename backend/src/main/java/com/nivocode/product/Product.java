
package com.nivocode.product;

import com.nivocode.company.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Product() {
    }

    public Product(Company company, String name, String sku,
                   String description, BigDecimal price,
                   Integer stockQuantity) {
        this.company = company;
        this.name = name;
        this.sku = sku;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void updateDetails(
            String name,
            String sku,
            String description,
            BigDecimal price,
            Integer stockQuantity
    ) {
        this.name = name;
        this.sku = sku;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
