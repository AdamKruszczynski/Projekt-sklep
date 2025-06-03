package org.example.service;

import org.example.entity.Company;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final CompanyRepository companyRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository, CompanyRepository companyRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public List<Product> getProductsByCompany(Company company) {
        return productRepository.findByCompany(company);
    }

    public void saveWithCompany(Product product, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Company company = companyRepository.findByOwner(user).orElseThrow();

        product.setCompany(company);
        productRepository.save(product);
    }

}
