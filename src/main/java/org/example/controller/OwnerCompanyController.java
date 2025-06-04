package org.example.controller;

import org.example.entity.Company;
import org.example.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/owner/company")
public class OwnerCompanyController {

    private final CompanyService companyService;

    public OwnerCompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/form")
    public String showCompanyForm(Principal principal, Model model) {
        Company company = companyService.getCompanyByCurrentUser(principal)
                .orElse(new Company());

        model.addAttribute("company", company);
        return "owner/company_form";
    }

    @PostMapping("/save")
    public String saveCompany(@ModelAttribute Company company, Principal principal) {
        companyService.saveOrUpdateCompany(company, principal);
        return "redirect:/owner/panel";
    }
}
