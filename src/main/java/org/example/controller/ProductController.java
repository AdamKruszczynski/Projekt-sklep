package org.example.controller;

import org.example.entity.Category;
import org.example.entity.Company;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.service.CategoryService;
import org.example.service.ProductService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CompanyRepository companyRepository;
    @Autowired
    private UserService userService;

    public ProductController(ProductService productService, CategoryService categoryService, CompanyRepository companyRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/owner/product/form")
    public String showProductForm(@RequestParam(required = false) Long id, Model model) {
        Product product = (id != null)
                ? productService.getById(id).orElse(new Product())
                : new Product();

        model.addAttribute("product", product);
        model.addAttribute("allCategories", categoryService.getAll());
        return "forms/product_form";
    }

    @PostMapping("/owner/product/save")
    public String saveProduct(@ModelAttribute Product product, Principal principal) {
        productService.saveWithCompany(product, principal);
        return "redirect:/owner/panel";
    }

    @GetMapping("/moderator/product/form")
    public String showProductFormForModerator(@RequestParam(required = false) Long id,
                                              Model model,
                                              Principal principal) {
        Product product = (id != null)
                ? productService.getById(id).orElse(new Product())
                : new Product();

        User moderator = userService.findByUsername(principal.getName()).orElseThrow();
        Company company = moderator.getCompany();

        List<Category> categories = categoryService.getCategoriesByCompany(company);

        model.addAttribute("product", product);
        model.addAttribute("allCategories", categories);
        return "forms/product_form";
    }

    @PostMapping("/moderator/product/save")
    public String saveProductForModerator(@ModelAttribute Product product, Principal principal) {
        User moderator = userService.findByUsername(principal.getName()).orElseThrow();
        Company company = companyRepository.findById(moderator.getCompany().getId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        product.setCompany(company);

        productService.save(product);
        return "redirect:/moderator/panel";
    }


}
