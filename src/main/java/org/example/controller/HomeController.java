package org.example.controller;

import org.example.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("companies", companyRepository.findAll());
        return "index";
    }

}