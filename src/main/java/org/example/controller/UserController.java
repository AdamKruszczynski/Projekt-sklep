package org.example.controller;

import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user,
                           Model model,
                           BCryptPasswordEncoder encoder) {
        boolean success = userService.register(user, encoder);

        if (!success) {
            model.addAttribute("error", "Nazwa użytkownika jest już zajęta.");
            return "register";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/user/user_panel")
    public String showUserPanel() {
        return "/user/user_panel";
    }

    @GetMapping("/user/account")
    public String showAccountOverview(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));
        model.addAttribute("user", user);
        return "user/account_overview";
    }

    @GetMapping("/user/edit")
    public String showEditForm(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));
        model.addAttribute("user", user);
        return "user/edit_form";
    }

    @PostMapping("/user/edit")
    public String updateUser(@ModelAttribute("user") User updatedUser, Principal principal, RedirectAttributes redirectAttributes) {
        userService.updateUserData(principal.getName(), updatedUser);
        redirectAttributes.addFlashAttribute("message", "Dane zostały zaktualizowane!");
        return "redirect:/user/account";
    }
}
