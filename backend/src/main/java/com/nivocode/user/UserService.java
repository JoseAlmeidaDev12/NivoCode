
package com.nivocode.user;

import com.nivocode.common.exception.ResourceConflictException;
import com.nivocode.company.Company;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createInitialAdmin(
            Company company,
            String name,
            String email,
            String password) {

        if (company == null || company.getId() == null || !company.isActive()) {
            throw new IllegalArgumentException("Invalid company");
        }

        if (name == null || name.isBlank() || name.trim().length() > 160) {
            throw new IllegalArgumentException("Invalid user name");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (normalizedEmail.length() > 254 ||
                !normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Invalid email");
        }

        if (password == null ||
                password.length() < 12 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Invalid password length");
        }

        if (userRepository.existsByCompany_IdAndEmailIgnoreCase(
                company.getId(), normalizedEmail)) {
            throw new ResourceConflictException("Email already registered");
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = new User(
                company,
                name.trim(),
                normalizedEmail,
                passwordHash,
                UserRole.ADMIN
        );

        return userRepository.save(user);
    }
}
