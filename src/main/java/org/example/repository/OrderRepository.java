package org.example.repository;

import org.example.entity.Company;
import org.example.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserUsername(String username);
    List<Order> findByCompanyId(Long companyId);
    List<Order> findByCompany(Company company);
}