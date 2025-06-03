package org.example.repository;

import org.example.entity.Company;
import org.example.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategories_Id(Long categoryId);
    List<Product> findByCompany(Company company);
}
