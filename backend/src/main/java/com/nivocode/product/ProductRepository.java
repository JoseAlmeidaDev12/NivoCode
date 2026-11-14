
package com.nivocode.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByCompany_Id(UUID companyId);

    Optional<Product> findByIdAndCompany_Id(
            UUID productId,
            UUID companyId
    );

    boolean existsByCompany_IdAndSku(
            UUID companyId,
            String sku
    );

    List<Product> findAllByCompany_IdAndActiveTrue(
            UUID companyId
    );

    Optional<Product> findByIdAndCompany_IdAndActiveTrue(
            UUID productId,
            UUID companyId
    );
}
