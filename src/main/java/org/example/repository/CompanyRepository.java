package org.example.repository;


import org.example.entity.Company;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByOwner(User owner);
    Optional<Company> findByOwner_Username(String username);
}
