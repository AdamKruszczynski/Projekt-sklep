package org.example.service;

import org.example.entity.Category;
import org.example.entity.Company;
import org.example.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Optional<Category> getById(Long id) {
        return categoryRepository.findById(id);
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public void save(Category category) {
        categoryRepository.save(category);
    }

    public List<Category> getCategoriesByCompany(Company company) {
        return categoryRepository.findByCompany(company);
    }
}
