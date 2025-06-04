package org.example.controller;

import org.example.entity.CartItem;
import org.example.entity.Product;
import org.example.repository.ProductRepository;
import org.example.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    private final ProductRepository productRepository;

    public CartController(CartService cartService, ProductRepository productRepository) {
        this.cartService = cartService;
        this.productRepository = productRepository;
    }

    @PostMapping("/")
    public String difficultPath(){
        return "redirect:/cart/view";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, Principal principal) {
        cartService.addProductToCart(principal.getName(), productId);

        Product product = productRepository.findById(productId).orElseThrow();
        Long companyId = product.getCompany().getId();

        return "redirect:/companies/" + companyId;
    }

    @PostMapping("/update")
    public ResponseEntity<Void> updateQuantity(@RequestParam Long productId, @RequestParam int quantity, Principal principal) {
        cartService.updateQuantity(principal.getName(), productId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId, Principal principal) {
        cartService.removeFromCart(principal.getName(), productId);
        return "redirect:/cart/view";
    }

    @GetMapping("/view")
    public String viewCart(Model model, Principal principal, HttpServletRequest request) {
        List<CartItem> cartItems = cartService.getCartItems(principal.getName());
        model.addAttribute("cartItems", cartItems);

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        model.addAttribute("total", total);

        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);

        return "cart";
    }

}
