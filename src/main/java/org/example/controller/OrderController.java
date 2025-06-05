package org.example.controller;

import org.example.entity.CartItem;
import org.example.entity.Order;
import org.example.service.CartService;
import org.example.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/history")
    public String orderHistory(Model model, Principal principal) {
        model.addAttribute("orders", orderService.getOrdersForUser(principal.getName()));
        return "/order/order-history";
    }

    @GetMapping("/confirm")
    public String showConfirmationPage(Model model, Principal principal) {
        String username = principal.getName();
        List<CartItem> cartItems = cartService.getCartItems(username);
        model.addAttribute("cartItems", cartItems);

        Double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();
        model.addAttribute("total", total);

        return "order/confirm";
    }

    @PostMapping("/submit")
    public String submitOrder(Principal principal, Model model) {
        Order order = orderService.createOrder(principal.getName());
        model.addAttribute("order", order);
        model.addAttribute("total", order.getTotal());
        return "order/confirm";
    }

    @PostMapping("/confirm/{orderId}")
    public String confirmOrder(@PathVariable Long orderId) {
        orderService.confirmOrder(orderId);
        return "redirect:/order/owner/orders";
    }

    @GetMapping("/cancel/{orderId}")
    public String showCancelForm(@PathVariable Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "order/cancel-form";
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, @RequestParam String reason) {
        orderService.cancelOrder(orderId, reason);
        return "redirect:/order/owner/orders";
    }

    @GetMapping("/owner/orders")
    public String getOrdersForOwner(Model model, Principal principal) {
        String username = principal.getName();
        List<Order> orders = orderService.getOrdersForOwner(username);
        model.addAttribute("orders", orders);
        return "order/owner-list";
    }

}
