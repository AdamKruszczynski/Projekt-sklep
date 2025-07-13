package org.example.controller;


import org.example.entity.Company;
import org.example.entity.Order;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.UserRepository;
import org.example.service.CompanyService;
import org.example.service.OrderService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    UserService userService;

    private final OrderService orderService;

    private final PasswordEncoder passwordEncoder;

    public OwnerController(UserRepository userRepository, CompanyService companyService, CompanyRepository companyRepository, OrderService orderService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyService = companyService;
        this.companyRepository = companyRepository;
        this.orderService = orderService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/moderator/form")
    public String showModeratorForm() {
        return "/owner/moderator_form";
    }

    @PostMapping("/moderator/form")
    public String addModerator(@RequestParam String username,
                               @RequestParam String password,
                               Principal principal) {

        Optional<User> optionalOwner = userService.findByUsername(principal.getName());
        if (optionalOwner.isEmpty()) {
            return "redirect:/error";
        }

        User owner = optionalOwner.get();

        Company company = companyRepository.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Owner has no company assigned"));

        User moderator = new User();
        moderator.setUsername(username);
        moderator.setPassword(password);
        moderator.setRole(User.Role.MODERATOR);
        moderator.setCompany(company);

        moderator.setEmail("placeholder@email.com");
        moderator.setPhoneNumber("000000000");

        userService.register(moderator, (BCryptPasswordEncoder) passwordEncoder);

        return "redirect:/owner/panel";
    }

    @GetMapping("/schedule")
    public String showSchedule(Principal principal, Model model) {
        Map<LocalDate, List<Order>> groupedOrders =
                orderService.getUpcomingOrdersForCompanyGrouped(principal.getName());

        model.addAttribute("groupedOrders", groupedOrders);
        return "/moderator/schedule";
    }


}