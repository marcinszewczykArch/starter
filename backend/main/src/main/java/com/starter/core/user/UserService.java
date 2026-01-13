package com.starter.core.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starter.core.exception.EmailAlreadyExistsException;

import java.util.Optional;

/** Service layer for User operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** Find user by email. */
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /** Find user by ID. */
    public Optional<User> findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id);
    }

    /** Check if email is already taken. */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Create a new user. Password should already be hashed.
     *
     * @throws EmailAlreadyExistsException if email is already taken
     */
    @Transactional
    public User createUser(String email, String hashedPassword, User.Role role) {
        log.info("Creating user with email: {}", email);

        // Early check for better error message (avoids DB exception in most cases)
        if (userRepository.existsByEmail(email)) {
            log.warn("Attempted to create user with existing email: {}", email);
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder().email(email).password(hashedPassword).role(role).build();

        try {
            return userRepository.save(user);
        } catch (DuplicateKeyException e) {
            // Race condition: another request inserted same email between check and save
            log.warn("Race condition: email {} was inserted by another request", email);
            throw new EmailAlreadyExistsException(email);
        }
    }
}
