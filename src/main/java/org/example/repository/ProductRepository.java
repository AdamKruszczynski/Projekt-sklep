package org.example.repository;

import org.example.entity.Company;
import org.example.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategories_Id(Long categoryId);
    List<Product> findByCompany(Company company);
    @Query("""
    SELECT p FROM Product p
    WHERE p.company.id = :companyId
      AND (:priceMin IS NULL OR p.price >= :priceMin)
      AND (:priceMax IS NULL OR p.price <= :priceMax)
    ORDER BY
      CASE WHEN :sortBy = 'priceAsc' THEN p.price END ASC,
      CASE WHEN :sortBy = 'priceDesc' THEN p.price END DESC
""")
    List<Product> findByCompanyWithFilters(@Param("companyId") Long companyId,
                                           @Param("priceMin") Double priceMin,
                                           @Param("priceMax") Double priceMax,
                                           @Param("sortBy") String sortBy);
}
