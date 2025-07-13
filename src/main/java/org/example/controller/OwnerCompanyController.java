package org.example.controller;

import org.example.entity.Company;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.CompanyService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final UserRepository userRepository;

    public OwnerCompanyController(CompanyService companyService, UserRepository userRepository) {
        this.companyService = companyService;
        this.userRepository = userRepository;
    }

    @GetMapping("/form")
    public String showCompanyForm(Principal principal, Model model) {
        Company company = companyService.getCompanyByCurrentUser(principal)
                .orElse(new Company());

        model.addAttribute("company", company);
        return "owner/company_form";
    }



    @PostMapping("/save")
    public String saveCompany(@ModelAttribute("company") Company company, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));

        companyService.saveOrUpdateCompany(company, principal);

        user.setCompany(company);
        userRepository.save(user);

        return "redirect:/owner/panel";
    }
}
