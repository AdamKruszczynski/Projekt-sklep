package org.example.controller;

import org.example.entity.Company;
import org.example.entity.Order;
import org.example.entity.User;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/moderator")
public class ModeratorController {

    @Autowired
    private UserService userService;

    private final OrderService orderService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CompanyService companyService;

    public ModeratorController(OrderService orderService, ProductService productService, CategoryService categoryService, CompanyService companyService) {
        this.orderService = orderService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.companyService = companyService;
    }

    @GetMapping("/panel")
    public String showModeratorPanel(Model model, Principal principal) {
        User moderator = userService.findByUsername(principal.getName()).orElseThrow();
        Company company = moderator.getCompany();

        model.addAttribute("company", company);
        model.addAttribute("categories", categoryService.getCategoriesByCompany(company));
        model.addAttribute("products", productService.getProductsByCompany(company));
        return "moderator/moderator_panel";
    }

    @GetMapping("/order/moderator/orders")
    public String redirectToOwnerOrders() {
        return "redirect:/order/owner/orders";
    }

    @GetMapping("/schedule")
    public String showSchedule(Principal principal, Model model) {
        Map<LocalDate, List<Order>> groupedOrders =
                orderService.getUpcomingOrdersForCompanyGrouped(principal.getName());

        model.addAttribute("groupedOrders", groupedOrders);
        return "/moderator/schedule";
    }
}
