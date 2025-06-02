package org.example.controller;


import org.example.entity.Company;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.UserRepository;
import org.example.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyService companyService;

//    @Autowired
//    private ProductService productService;

    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Company company = user.getCompany();
        if (company == null) {
            company = new Company();
        }
        model.addAttribute("company", company);
        return "owner/dashboard";
    }

    @PostMapping("/company/save")
    public String saveCompany(@ModelAttribute("company") Company company, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Company existing = user.getCompany();

        if (existing != null) {
            company.setOwner(user);
            companyRepository.save(company);
        }else {
            company.setOwner(user);
            companyService.createNewCompanyForUser(company, user);

        }

        return "redirect:/owner/dashboard";
    }
}