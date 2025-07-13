package org.example.controller;

import org.example.entity.CartItem;
import org.example.entity.Order;
import org.example.service.CartService;
import org.example.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    public String submitOrder(@RequestParam("pickupTime")
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                              LocalDateTime pickupTime,
                              Principal principal,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        List<CartItem> cartItems = cartService.getCartItems(principal.getName());

        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Twój koszyk jest pusty. Dodaj produkty przed złożeniem zamówienia.");
            return "redirect:/cart/view";
        }

        Order order = orderService.createOrder(principal.getName(), pickupTime);
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
        List<Order> orders = orderService.getOrdersForCompany(username);
        model.addAttribute("orders", orders);
        return "order/owner-list";
    }

    @GetMapping("/schedule")
    public String showSchedule(Principal principal, Model model) {
        Map<LocalDate, List<Order>> groupedOrders =
                orderService.getUpcomingOrdersGroupedByDate(principal.getName());

        groupedOrders.values().stream()
                .flatMap(List::stream)
                .forEach(order -> order.getItems().size());

        model.addAttribute("groupedOrders", groupedOrders);
        return "user/schedule";
    }

}
