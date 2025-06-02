package org.example.controller;

import org.example.entity.Company;
import org.example.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

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
        model.addAttribute("company", company);
        return "company";
    }
}