package org.example.controller;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToOne;
import org.example.entity.Category;
import org.example.entity.Company;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.service.CategoryService;
import org.example.service.CompanyService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class CategoryController {

    @Autowired
    private UserService userService;

    private final CategoryService categoryService;
    private final CompanyService companyService;
    private final CompanyRepository companyRepository;

    public CategoryController(CategoryService categoryService, CompanyService companyService, CompanyRepository companyRepository) {
        this.categoryService = categoryService;
        this.companyService = companyService;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/owner/category/form")
    public String showCategoryForm(@RequestParam(required = false) Long id, Model model, Principal principal) {
        Category category = (id != null)
                ? categoryService.getById(id).orElse(new Category())
                : new Category();


        Company company = companyService.getCompanyByCurrentUser(principal)
                .orElse(new Company());

        category.setCompany(company);

        model.addAttribute("category", category);
        return "forms/category_form";
    }

    @PostMapping("/owner/category/save")
    public String saveCategory(@ModelAttribute Category category) {
        categoryService.save(category);
        return "redirect:/owner/panel";
    }

    @GetMapping("/moderator/category/form")
    public String showCategoryFormForModerator(@RequestParam(required = false) Long id,
                                   Model model, Principal principal) {
        Category category = (id != null) ? categoryService.getById(id).orElse(new Category()) : new Category();

        User moderator = userService.findByUsername(principal.getName()).orElseThrow();
        Company company = moderator.getCompany();

        model.addAttribute("category", category);
        model.addAttribute("company", company);
        return "forms/category_form";
    }

    @PostMapping("/moderator/category/save")
    public String saveCategory(@ModelAttribute Category category, Principal principal) {
        User moderator = userService.findByUsername(principal.getName()).orElseThrow();

        Company company = companyRepository.findById(moderator.getCompany().getId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        category.setCompany(company);
        categoryService.save(category);
        return "redirect:/moderator/panel";
    }
}
