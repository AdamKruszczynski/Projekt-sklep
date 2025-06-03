package org.example.controller;

import org.example.entity.Category;
import org.example.entity.Company;
import org.example.repository.CategoryRepository;
import org.example.service.CategoryService;
import org.example.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/owner/category")
public class CategoryController {

    private final CategoryService categoryService;
    private final CompanyService companyService;

    public CategoryController(CategoryService categoryService, CompanyService companyService) {
        this.categoryService = categoryService;
        this.companyService = companyService;
    }

    @GetMapping("/form")
    public String showCategoryForm(@RequestParam(required = false) Long id, Model model, Principal principal) {
        Category category = (id != null)
                ? categoryService.getById(id).orElse(new Category())
                : new Category();

        Company company = companyService.getCompanyByCurrentUser(principal);
        category.setCompany(company);

        model.addAttribute("category", category);
        return "owner/category_form";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category) {
        categoryService.save(category);
        return "redirect:/owner/panel";
    }
}
