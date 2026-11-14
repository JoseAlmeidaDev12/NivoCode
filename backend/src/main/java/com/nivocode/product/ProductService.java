
package com.nivocode.product;

import com.nivocode.common.exception.ResourceConflictException;
import com.nivocode.common.security.CurrentTenantProvider;
import com.nivocode.company.Company;
import com.nivocode.company.CompanyRepository;
import com.nivocode.product.dto.ProductRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final CurrentTenantProvider currentTenantProvider;

    public ProductService(
            ProductRepository productRepository,
            CompanyRepository companyRepository,
            CurrentTenantProvider currentTenantProvider
    ) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        UUID companyId = currentTenantProvider.getCompanyId();

        return productRepository.findAllByCompany_Id(companyId);
    }

    @Transactional
    public Product create(ProductRequest request) {
        UUID companyId = currentTenantProvider.getCompanyId();

        String name = request.name().trim();
        String sku = request.sku().trim();

        if (productRepository.existsByCompany_IdAndSku(companyId, sku)) {
            throw new ResourceConflictException(
                    "Product SKU already exists in this company"
            );
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated company not found"
                ));

        Product product = new Product(
                company,
                name,
                sku,
                request.description(),
                request.price(),
                request.stockQuantity()
        );

        return productRepository.save(product);
    }
}
