
package com.nivocode.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCompany_IdAndEmailIgnoreCase(
            UUID companyId,
            String email
    );

    boolean existsByCompany_IdAndEmailIgnoreCase(
            UUID companyId,
            String email
    );
}
