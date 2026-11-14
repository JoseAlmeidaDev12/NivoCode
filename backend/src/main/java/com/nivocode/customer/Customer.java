
package com.nivocode.customer;

import com.nivocode.company.Company;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 18)
    private String document;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private boolean active = true;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected Customer() {
    }

    public Customer(
            Company company,
            String name,
            String email,
            String phone,
            String document,
            String address
    ) {
        this.company = company;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.document = document;
        this.address = address;
    }

    public void updateDetails(
            String name,
            String email,
            String phone,
            String document,
            String address
    ) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.document = document;
        this.address = address;
    }

    public void deactivate() {
        this.active = false;
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

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDocument() {
        return document;
    }

    public String getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
