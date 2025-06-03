package org.example.controller;

import org.example.entity.Product;
import org.example.service.CategoryService;
import org.example.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/owner/product")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/form")
    public String showProductForm(@RequestParam(required = false) Long id, Model model) {
        Product product = (id != null)
                ? productService.getById(id).orElse(new Product())
                : new Product();

        model.addAttribute("product", product);
        model.addAttribute("allCategories", categoryService.getAll());
        return "owner/product_form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product, Principal principal) {
        productService.saveWithCompany(product, principal);
        return "redirect:/owner/panel";
    }
}
