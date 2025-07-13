package org.example.controller;

import org.example.entity.CartItem;
import org.example.entity.Product;
import org.example.entity.SavedCart;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        LocalDateTime tomorrowStart = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        String minDate = tomorrowStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        model.addAttribute("minDate", minDate);

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        model.addAttribute("total", total);

        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);

        return "cart";
    }

    @PostMapping("/save")
    public String saveCart(@RequestParam("name") String name, Principal principal, RedirectAttributes redirectAttributes) {
        cartService.saveCurrentCart(name, principal.getName());
        redirectAttributes.addFlashAttribute("message", "Koszyk został zapisany!");
        return "redirect:/cart/view";
    }

    @PostMapping("/load")
    public String loadSavedCart(@RequestParam Long cartId, Principal principal) {
        try {
            cartService.loadSavedCart(cartId, principal.getName());
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/cart/view";
    }

    @GetMapping("/saved")
    public String showSavedCarts(Principal principal, Model model) {
        List<SavedCart> savedCarts = cartService.getSavedCarts(principal.getName());
        model.addAttribute("savedCarts", savedCarts);
        return "/saved_carts";
    }

    @PostMapping("/rename")
    public String renameSavedCart(@RequestParam Long cartId,
                                  @RequestParam String name,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        cartService.renameSavedCart(cartId, name, principal.getName());
        redirectAttributes.addFlashAttribute("message", "Zmieniono nazwę koszyka.");
        return "redirect:/cart/saved";
    }

    @PostMapping("/saved/removeProduct")
    public String removeProductFromSavedCart(@RequestParam Long cartId,
                                             @RequestParam Long productId,
                                             Principal principal) {
        cartService.removeProductFromSavedCart(cartId, productId, principal.getName());
        return "redirect:/cart/saved/details?cartId=" + cartId;
    }

    @PostMapping("/delete")
    public String deleteSavedCart(@RequestParam Long cartId,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        cartService.deleteSavedCart(cartId, principal.getName());
        redirectAttributes.addFlashAttribute("message", "Koszyk został usunięty.");
        return "redirect:/cart/saved";
    }

    @PostMapping("/saved/remove-product")
    public String removeProductFromSavedCart(@RequestParam Long cartId,
                                             @RequestParam Long productId,
                                             Principal principal,
                                             RedirectAttributes redirectAttributes) {
        cartService.removeProductFromSavedCart(cartId, productId, principal.getName());
        redirectAttributes.addFlashAttribute("message", "Produkt został usunięty z koszyka.");
        return "redirect:/cart/saved";
    }


}
