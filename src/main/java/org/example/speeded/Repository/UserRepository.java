package org.example.speeded.Repository;

import org.example.speeded.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//public interface UserRepository extends JpaRepository<User, Long> {
//}

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
