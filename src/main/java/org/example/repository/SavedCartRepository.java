package org.example.repository;

import org.example.entity.SavedCart;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedCartRepository extends JpaRepository<SavedCart, Long> {
    List<SavedCart> findByUser(User user);
    List<SavedCart> findAllByUserUsername(String username);
    Optional<SavedCart> findByIdAndUserUsername(Long id, String username);
}
