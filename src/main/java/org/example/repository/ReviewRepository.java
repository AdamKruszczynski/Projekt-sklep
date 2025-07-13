package org.example.repository;

import org.example.entity.Company;
import org.example.entity.Review;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByUserAndCompany(User user, Company company);
    List<Review> findByCompanyOrderByCreatedAtDesc(Company company);
    boolean existsByUserAndCompany(User user, Company company);
}
