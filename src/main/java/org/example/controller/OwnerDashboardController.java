package org.example.controller;

import org.example.entity.Company;
import org.example.service.CategoryService;
import org.example.service.CompanyService;
import org.example.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/owner/panel")
public class OwnerDashboardController {

    private final CompanyService companyService;
    private final CategoryService categoryService;
    private final ProductService productService;

    public OwnerDashboardController(CompanyService companyService,
                                    CategoryService categoryService,
                                    ProductService productService) {
        this.companyService = companyService;
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping
    public String showOwnerDashboard(Model model, Principal principal) {
        Optional<Company> optionalCompany = companyService.getCompanyByCurrentUser(principal);

        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.get();
            model.addAttribute("company", company);
            model.addAttribute("categories", categoryService.getCategoriesByCompany(company));
            model.addAttribute("products", productService.getProductsByCompany(company));
        } else {
            model.addAttribute("company", null);
        }

        return "owner/owner_dashboard";
    }
}
