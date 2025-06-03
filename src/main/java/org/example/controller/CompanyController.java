package org.example.controller;

import org.example.entity.Category;
import org.example.entity.Company;
import org.example.entity.Product;
import org.example.repository.CategoryRepository;
import org.example.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    private CategoryRepository categoryRepository;

//    @Autowired
//    private ProductService productService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public String showCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());
        return "companies"; // plik szablonu np. companies.html
    }

    @GetMapping("/{id}")
    public String showCompany(@PathVariable Long id, Model model) {
        Company company = companyService.getCompanyById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono firmy o ID: " + id));

        List<Category> categories = categoryRepository.findByCompany(company);

        Map<Category, List<Product>> categoryProducts = new LinkedHashMap<>();
        for (Category category : categories) {
            List<Product> filteredProducts = category.getProducts().stream()
                    .filter(p -> p.getCompany() != null && p.getCompany().getId().equals(company.getId()))
                    .toList();
            categoryProducts.put(category, filteredProducts);
        }

        model.addAttribute("company", company);
        model.addAttribute("categoryProducts", categoryProducts);
        return "company";
    }


}