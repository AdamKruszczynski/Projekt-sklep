package org.example.controller;

import org.example.service.CompanyService;
import org.example.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final CompanyService companyService;

    public AdminController(UserService userService, CompanyService companyService) {
        this.userService = userService;
        this.companyService = companyService;
    }

    @GetMapping("/panel")
    public String showAdminPanel(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("companies", companyService.getAllCompanies());
        return "admin/admin_panel";
    }

    @PostMapping("/change-role")
    public String changeUserRole(@RequestParam Long userId, @RequestParam String newRole) {
        userService.updateUserRole(userId, newRole);
        return "redirect:/admin/panel";
    }

    @PostMapping("/toggle-company")
    public String toggleCompany(@RequestParam Long companyId) {
        companyService.toggleCompanyActive(companyId);
        return "redirect:/admin/panel";
    }

    @PostMapping("/users/{id}/toggle-enabled")
    public String toggleUserEnabled(@PathVariable Long id) {
        userService.toggleUserEnabled(id);
        return "redirect:/admin/panel";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/panel";
    }
}
