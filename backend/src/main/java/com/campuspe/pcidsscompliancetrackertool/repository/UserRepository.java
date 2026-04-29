package com.campuspe.pcidsscompliancetrackertool.repository;

import com.campuspe.pcidsscompliancetrackertool.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the login name to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param username the login name to check
     * @return {@code true} if a user with that username exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to check
     * @return {@code true} if a user with that email exists
     */
    boolean existsByEmail(String email);
}
